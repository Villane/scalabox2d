package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._

object BroadPhase {
  val Invalid = Int.MaxValue
  val NullEdge = Int.MaxValue
  val Validate = false
  private val DebugPrint = false

  def binarySearch(bounds: Array[Bound], count: Int, value: Int): Int = {
    /* Would it only be possible to use  comparator and java.util.Arrays#binarySearch
        val comparator = new java.util.Comparator[Bound] {
          def compare(b1: Bound, b2: Bound) = b1.value - b2.value
        }*/
    if (DebugPrint) {
      System.out.println("BinarySearch()");
    }
    var low = 0
    var high = count - 1
    while (low <= high) {
      val mid = (low + high) >> 1
      if (bounds(mid).value > value) {
        high = mid - 1;
      } else if (bounds(mid).value < value) {
        low = mid + 1;
      } else {
        return mid;
      }
    }
    return low;
  }
}

class BoundValues {
  val lowerValues = new Array[Int](2)
  val upperValues = new Array[Int](2)
}

// Notes:
// - we use bound arrays instead of linked lists for cache coherence.
// - we use quantized integral values for fast compares.
// - we use short indices rather than pointers to save memory.
// - we use a stabbing count for fast overlap queries (less than order N).
// - we also use a time stamp on each proxy to speed up the registration of
// overlap query results.
// - where possible, we compare bound indices instead of values to reduce
// cache misses (TODO_ERIN).
// - no broadphase is perfect and neither is this one: it is not great for
// huge worlds (use a multi-SAP instead), it is not great for large objects.

/**
 * This broad phase uses the Sweep and Prune algorithm as described in:
 * Collision Detection in Interactive 3D Environments by Gino van den Bergen
 * Also, some ideas, such as using integral values for fast compares comes from
 * Bullet (http:/www.bulletphysics.com).
 */
class BroadPhase(val worldAABB: AABB, callback: PairListener) {
  val pairManager = new PairManager(this, callback)
  
  val proxyPool = Array.fromFunction(i => {
                                       val p = new Proxy
                                       p.next = i + 1
                                       p
                                     })(Settings.maxProxies)
  var proxyCount = 0
  var freeProxy = 0
  
  val pairBuffer = new Array[(Int,Int)](Settings.maxPairs)
  var pairBufferCount = 0

  val m_bounds = Array.fromFunction((i,j) => Bound(0,0))(2, 2 * Settings.maxProxies)
  val queryResults = new Array[Int](Settings.maxProxies)
  var queryResultCount = 0
  
  val quantizationFactor = Int.MaxValue.toFloat / (worldAABB.upperBound - worldAABB.lowerBound)
  var timeStamp = 1

  init
  
  def init {
    if (BroadPhase.DebugPrint) {
      System.out.println("BroadPhase()");
    }

    assert(worldAABB.isValid)

    proxyPool(Settings.maxProxies - 1).next = PairManager.NullProxy
  }

  def testOverlap(p1: Proxy, p2: Proxy): Boolean = {
    var axis = 0
    while (axis <= 1) {
      val bounds = m_bounds(axis)

      assert(p1.lowerBounds(axis) < 2 * proxyCount)
      assert(p1.upperBounds(axis) < 2 * proxyCount)
      assert(p2.lowerBounds(axis) < 2 * proxyCount)
      assert(p2.upperBounds(axis) < 2 * proxyCount)

      if (bounds(p1.lowerBounds(axis)).value > bounds(p2.upperBounds(axis)).value)
        return false

      if (bounds(p1.upperBounds(axis)).value < bounds(p2.lowerBounds(axis)).value)
        return false
      axis += 1
    }

    return true
  }

  def testOverlap(b: BoundValues, p: Proxy): Boolean = {
    var axis = 0
    while (axis <= 1) {
      val bounds = m_bounds(axis)

      assert(p.lowerBounds(axis) < 2 * proxyCount)
      assert(p.upperBounds(axis) < 2 * proxyCount)

      if (b.lowerValues(axis) > bounds(p.upperBounds(axis)).value)
        return false

      if (b.upperValues(axis) < bounds(p.lowerBounds(axis)).value)
        return false
      axis += 1
    }

    return true
  }

  def getProxy(proxyId: Int) = {
    if (proxyId == PairManager.NullProxy || !proxyPool(proxyId).isValid) {
      None
    } else {
      Some(proxyPool(proxyId))
    }
  }

    // Create and destroy proxies. These call Flush first.
    def createProxy(aabb: AABB, //int groupIndex, int categoryBits, int maskBits,
            userData: AnyRef): Int = {
        if (BroadPhase.DebugPrint) {
            System.out.println("CreateProxy()");
        }
        
        assert(proxyCount < Settings.maxProxies)
        assert(freeProxy != PairManager.NullProxy)
        
        val proxyId = freeProxy
        val proxy = proxyPool(proxyId)
        freeProxy = proxy.next

        proxy.overlapCount = 0
        proxy.userData = userData
//        proxy.groupIndex = groupIndex;
//        proxy.categoryBits = categoryBits;
//        proxy.maskBits = maskBits;
        
        //assert m_proxyCount < Settings.maxProxies;

        val boundCount = 2 * proxyCount

        val lowerValues, upperValues = new Array[Int](2)
        computeBounds(lowerValues, upperValues, aabb)

        for (axis <- 0 to 1) {
            val bounds = m_bounds(axis)
            val indexes = new Array[Int](2)

            query(indexes, lowerValues(axis), upperValues(axis), bounds,
                    boundCount, axis)
            val lowerIndex = indexes(0)
            var upperIndex = indexes(1)

            // System.out.println(edgeCount + ", "+lowerValues[axis] + ",
            // "+upperValues[axis]);
            // memmove(bounds[upperIndex + 2], bounds[upperIndex],
            // (edgeCount - upperIndex) * sizeof(b2Bound));

            System.arraycopy(m_bounds(axis), upperIndex, m_bounds(axis),
                    upperIndex + 2, boundCount - upperIndex)
            var i = 0
            while (i < boundCount - upperIndex) {
                m_bounds(axis)(upperIndex + 2 + i) = new Bound(m_bounds(axis)(upperIndex + 2 + i))
                i += 1
            }

            // memmove(bounds[lowerIndex + 1], bounds[lowerIndex],
            // (upperIndex - lowerIndex) * sizeof(b2Bound));
            // System.out.println(lowerIndex+" "+upperIndex);
            System.arraycopy(m_bounds(axis), lowerIndex, m_bounds(axis),
                    lowerIndex + 1, upperIndex - lowerIndex);
            i = 0
            while (i < upperIndex - lowerIndex) {
                m_bounds(axis)(lowerIndex + 1 + i) = new Bound(
                        m_bounds(axis)(lowerIndex + 1 + i))
                i += 1
            }

            // The upper index has increased because of the lower bound
            // insertion.
            upperIndex += 1

            // Copy in the new bounds.

            // if (bounds[lowerIndex] == null)
            assert (bounds(lowerIndex) != null, "Null pointer (lower)")
            // if (bounds[upperIndex] == null)
            assert (bounds(upperIndex) != null, "Null pointer (upper)")

            bounds(lowerIndex).value = lowerValues(axis)
            bounds(lowerIndex).proxyId = proxyId
            bounds(upperIndex).value = upperValues(axis)
            bounds(upperIndex).proxyId = proxyId

            bounds(lowerIndex).stabbingCount = if (lowerIndex == 0) 0
                    else bounds(lowerIndex - 1).stabbingCount
            bounds(upperIndex).stabbingCount = bounds(upperIndex - 1).stabbingCount

            // System.out.printf("lv: %d , lid: %d, uv: %d, uid: %d
            // \n",lowerValues[axis],proxyId,upperValues[axis],proxyId);

            // Adjust the stabbing count between the new bounds.
            var index = lowerIndex
            while (index < upperIndex) {
                bounds(index).stabbingCount += 1
                index += 1
            }

            // Adjust the all the affected bound indices.
            index = lowerIndex
            while (index < boundCount + 2) {
                val proxyn = proxyPool(bounds(index).proxyId)
                if (bounds(index).isLower) {
                    proxyn.lowerBounds(axis) = index
                }
                else {
                    proxyn.upperBounds(axis) = index
                }
                index += 1
            }
        }

        proxyCount += 1

        assert(queryResultCount < Settings.maxProxies)
        // Create pairs if the AABB is in range.
        var i = 0
        while (i < queryResultCount) {
            assert(queryResults(i) < Settings.maxProxies)
            assert(proxyPool(queryResults(i)).isValid)

            pairManager.addBufferedPair(proxyId, queryResults(i))
            i += 1
        }

        pairManager.commit()

        if (BroadPhase.Validate)
        {
            validate()
        }

        // Prepare for next query.
        queryResultCount = 0
        incrementTimeStamp()

        return proxyId
    }

    def destroyProxy(proxyId: Int) {
        assert(0 < proxyCount && proxyCount <= Settings.maxProxies);
        val proxy = proxyPool(proxyId)
        assert(proxy.isValid)

        val boundCount = 2 * proxyCount

        for (axis <- 0 to 1) {
            val bounds = m_bounds(axis)

            val lowerIndex = proxy.lowerBounds(axis);
            val upperIndex = proxy.upperBounds(axis);
            val lowerValue = bounds(lowerIndex).value;
            val upperValue = bounds(upperIndex).value;

            // memmove(bounds + lowerIndex, bounds + lowerIndex + 1,
            // (upperIndex - lowerIndex - 1) * sizeof(b2Bound));
            // memmove(bounds[lowerIndex + 1], bounds[lowerIndex],
            // (upperIndex - lowerIndex) * sizeof(b2Bound));
            System.arraycopy(m_bounds(axis), lowerIndex + 1, m_bounds(axis),
                    lowerIndex, upperIndex - lowerIndex - 1);
            for (i <- 0 until upperIndex - lowerIndex - 1) {
                bounds(lowerIndex + i) = new Bound(bounds(lowerIndex + i))
            }
            // memmove(bounds + upperIndex-1, bounds + upperIndex + 1,
            // (edgeCount - upperIndex - 1) * sizeof(b2Bound));
            System.arraycopy(m_bounds(axis), upperIndex + 1, m_bounds(axis),
                    upperIndex - 1, boundCount - upperIndex - 1);
            var i = 0
            while (i < boundCount - upperIndex - 1) {
                bounds(upperIndex - 1 + i) = new Bound(bounds(upperIndex - 1 + i));
                i += 1
            }

            // Fix bound indices.
            var index = lowerIndex
            while (index < boundCount - 2) {
                val proxyn = proxyPool(bounds(index).proxyId)
                if (bounds(index).isLower) {
                    proxyn.lowerBounds(axis) = index
                }
                else {
                    proxyn.upperBounds(axis) = index
                }
                index += 1
            }

            // Fix stabbing count.
            index = lowerIndex
            while (index < upperIndex - 1) {
                bounds(index).stabbingCount -= 1
                index += 1
            }

            // Query for pairs to be removed. lowerIndex and upperIndex are not
            // needed.
            val ignored = new Array[Int](2)
            query(ignored, lowerValue, upperValue, bounds, boundCount - 2, axis)
        }

        assert (queryResultCount < Settings.maxProxies)

        var i = 0
        while (i < queryResultCount) {
            assert(proxyPool(queryResults(i)).isValid)
            pairManager.removeBufferedPair(proxyId, queryResults(i))
            i += 1
        }
        
        pairManager.commit()

        // Prepare for next query.
        queryResultCount = 0
        incrementTimeStamp()

        // Return the proxy to the pool.
        proxy.userData = null;
        proxy.overlapCount = BroadPhase.Invalid
        proxy.lowerBounds(0) = BroadPhase.Invalid
        proxy.lowerBounds(1) = BroadPhase.Invalid
        proxy.upperBounds(0) = BroadPhase.Invalid
        proxy.upperBounds(1) = BroadPhase.Invalid


        // Return the proxy to the pool.
        proxy.next = freeProxy
        freeProxy = proxyId;
        proxyCount -= 1

         if (BroadPhase.Validate) {
             validate();
         }
    }

    // Call MoveProxy as many times as you like, then when you are done
    // call Flush to finalized the proxy pairs (for your time step).
    def moveProxy(proxyId: Int, aabb: AABB) {
        if (BroadPhase.DebugPrint) {
            System.out.println("MoveProxy()");
        }

        if (proxyId == PairManager.NullProxy || Settings.maxProxies <= proxyId) {
            return;
        }

        assert (aabb.isValid, "invalid AABB")

        val boundCount = 2 * proxyCount

        val proxy = proxyPool(proxyId)
        
        //Get new bound values
        val newValues = new BoundValues
        computeBounds(newValues.lowerValues, newValues.upperValues, aabb)
        
        //Get old bound values
        val oldValues = new BoundValues
        for (axis <- 0 to 1) {
            oldValues.lowerValues(axis) = m_bounds(axis)(proxy.lowerBounds(axis)).value
            oldValues.upperValues(axis) = m_bounds(axis)(proxy.upperBounds(axis)).value
        }

        for (axis <- 0 to 1) {
            val bounds = m_bounds(axis)

            val lowerIndex = proxy.lowerBounds(axis)
            val upperIndex = proxy.upperBounds(axis)

            val lowerValue = newValues.lowerValues(axis)
            val upperValue = newValues.upperValues(axis)

            val deltaLower = lowerValue - bounds(lowerIndex).value
            val deltaUpper = upperValue - bounds(upperIndex).value

            bounds(lowerIndex).value = lowerValue
            bounds(upperIndex).value = upperValue

            //
            // Expanding adds overlaps
            //

            // Should we move the lower bound down?
            if (deltaLower < 0) {
                var index = lowerIndex;
                while (index > 0 && lowerValue < bounds(index - 1).value) {
                    val bound = bounds(index)
                    val prevBound = bounds(index - 1)

                    val prevProxyId = prevBound.proxyId;
                    val prevProxy = proxyPool(prevBound.proxyId)

                    prevBound.stabbingCount += 1

                    if (prevBound.isUpper) {
                        if (testOverlap(newValues, prevProxy)) {
                            pairManager.addBufferedPair(proxyId, prevProxyId);
                        }

                        prevProxy.upperBounds(axis) += 1
                        bound.stabbingCount += 1
                    }
                    else {
                        prevProxy.lowerBounds(axis) += 1
                        bound.stabbingCount -= 1
                    }

                    proxy.lowerBounds(axis) -= 1

                    // b2Swap(*bound, *prevEdge);
                    // TODO perhaps have to make a copy here? maybe not
                    val tmp = bounds(index) // bound
                    bounds(index) = bounds(index - 1) // prevBound
                    bounds(index - 1) = tmp
                    index -= 1
                }
            }

            // Should we move the upper bound up?
            if (deltaUpper > 0) {
                var index = upperIndex
                while (index < boundCount - 1
                        && bounds(index + 1).value <= upperValue) {
                    val bound = bounds(index)
                    val nextBound = bounds(index + 1)
                    val nextProxyId = nextBound.proxyId
                    val nextProxy = proxyPool(nextProxyId)

                    nextBound.stabbingCount += 1

                    if (nextBound.isLower) {
                        if (testOverlap(newValues, nextProxy)) {
                            pairManager.addBufferedPair(proxyId, nextProxyId)
                        }

                        nextProxy.lowerBounds(axis) -= 1
                        bound.stabbingCount += 1
                    }
                    else {
                        nextProxy.upperBounds(axis) -= 1
                        bound.stabbingCount -= 1
                    }

                    proxy.upperBounds(axis) += 1
                    // b2Swap(*bound, *nextEdge);
                    // wasn't actually swapping! bounds[index] and
                    // bounds[index+1] need to be swapped by VALUE
                    // TODO perhaps have to make a copy here? maybe not
                    val tmp = bounds(index) // bound
                    bounds(index) = bounds(index + 1) // nextBound
                    bounds(index + 1) = tmp
                    index += 1
                }
            }

            //
            // Shrinking removes overlaps
            //

            // Should we move the lower bound up?
            if (deltaLower > 0) {
                var index = lowerIndex;
                while (index < boundCount - 1
                        && bounds(index + 1).value <= lowerValue) {
                    val bound = bounds(index)
                    val nextBound = bounds(index + 1)

                    val nextProxyId = nextBound.proxyId;
                    val nextProxy = proxyPool(nextProxyId)

                    nextBound.stabbingCount -= 1

                    if (nextBound.isUpper) {
                        if (testOverlap(oldValues,nextProxy)) {
                            pairManager.removeBufferedPair(proxyId, nextProxyId);
                        }

                        nextProxy.upperBounds(axis) -= 1
                        bound.stabbingCount -= 1
                    }
                    else {
                        nextProxy.lowerBounds(axis) -= 1
                        bound.stabbingCount += 1
                    }

                    proxy.lowerBounds(axis) += 1
                    // b2Swap(*bound, *nextEdge);
                    // Bound tmp = bound;
                    // bound = nextEdge;
                    // nextEdge = tmp;
                    val tmp = bounds(index) // bound
                    bounds(index) = bounds(index + 1) // nextBound
                    bounds(index + 1) = tmp
                    index += 1
                }
            }

            // Should we move the upper bound down?
            if (deltaUpper < 0) {
                var index = upperIndex;
                while (index > 0 && upperValue < bounds(index - 1).value) {
                    val bound = bounds(index)
                    val prevBound = bounds(index - 1)

                    val prevProxyId = prevBound.proxyId;
                    val prevProxy = proxyPool(prevProxyId)

                    prevBound.stabbingCount -= 1

                    if (prevBound.isLower) {
                        if (testOverlap(oldValues, prevProxy)) {
                            pairManager.removeBufferedPair(proxyId, prevProxyId)
                        }

                        prevProxy.lowerBounds(axis) += 1
                        bound.stabbingCount -= 1
                    }
                    else {
                        prevProxy.upperBounds(axis) += 1
                        bound.stabbingCount += 1
                    }

                    proxy.upperBounds(axis) -= 1
                    // b2Swap(*bound, *prevEdge);
                    // Bound tmp = bound;
                    // bound = prevEdge;
                    // prevEdge = tmp;
                    val tmp = bounds(index) // bound
                    bounds(index) = bounds(index - 1) // prevBound
                    bounds(index - 1) = tmp
                    index -= 1
                }
            }
        }

        if (BroadPhase.Validate) {
            validate();
        }
    }

  def commit() {
    pairManager.commit()
  }

    /**
     * Query an AABB for overlapping proxies, returns the user data and
     * the count, up to the supplied maximum count.
     */
    def query(aabb: AABB, maxCount: Int): Array[AnyRef] = {
        if (BroadPhase.DebugPrint) {
            System.out.println("Query(2 args)");
        }

        val lowerValues = new Array[Int](2)
        val upperValues = new Array[Int](2)
        computeBounds(lowerValues, upperValues, aabb)

        val indexes = new Array[Int](2) // lowerIndex, upperIndex;

        query(indexes, lowerValues(0), upperValues(0), m_bounds(0),
                2 * proxyCount, 0)
        query(indexes, lowerValues(1), upperValues(1), m_bounds(1),
                2 * proxyCount, 1)

        assert(queryResultCount < Settings.maxProxies)

        val results = new Array[AnyRef](maxCount)

        var count = 0
        var i = 0
        while (i < queryResultCount && count < maxCount) {
            assert(queryResults(i) < Settings.maxProxies)
            val proxy = proxyPool(queryResults(i))
            proxy.isValid
            results(i) = proxy.userData
            i += 1
            count += 1
        }
        
        val copy = new Array[AnyRef](count)
        System.arraycopy(results,0,copy,0,count)

        // Prepare for next query.
        queryResultCount = 0
        incrementTimeStamp()

        return copy//results
    }

      def validate() {
        if (BroadPhase.DebugPrint) {
            System.out.println("Validate()");
        }

        for (axis <- 0 to 1) {
            val bounds = m_bounds(axis)

            val boundCount = 2 * proxyCount
            var stabbingCount = 0

            var i = 0
            while (i < boundCount) {
                val bound = bounds(i)
                assert(i == 0 || bounds(i-1).value <= bound.value)
                assert(bound.proxyId != PairManager.NullProxy)
                assert(proxyPool(bound.proxyId).isValid)


                if (bound.isLower) {
                    assert (proxyPool(bound.proxyId).lowerBounds(axis) == i, (proxyPool(bound.proxyId).lowerBounds(axis)
                            + " not " + i))
                    stabbingCount += 1
                }
                else {
                    assert (proxyPool(bound.proxyId).upperBounds(axis) == i)
                    stabbingCount -= 1
                }

                assert (bound.stabbingCount == stabbingCount);
                i += 1
            }
        }

    }


  def computeBounds(lowerValues: Array[Int], upperValues: Array[Int], aabb: AABB) {
        if (BroadPhase.DebugPrint) {
            System.out.println("ComputeBounds()");
        }
        assert(aabb.upperBound.x > aabb.lowerBound.x)
        assert(aabb.upperBound.y > aabb.lowerBound.y)
        
        val minVertex = aabb.lowerBound.clamp(worldAABB.lowerBound, worldAABB.upperBound)
        val maxVertex = aabb.upperBound.clamp(worldAABB.lowerBound, worldAABB.upperBound)

        // System.out.printf("minV = %f %f, maxV = %f %f
        // \n",aabb.minVertex.x,aabb.minVertex.y,aabb.maxVertex.x,aabb.maxVertex.y);

        // Bump lower bounds downs and upper bounds up. This ensures correct
        // sorting of
        // lower/upper bounds that would have equal values.
        // TODO_ERIN implement fast float to int conversion.
        val vl = (minVertex - worldAABB.lowerBound)
        val vu = (maxVertex - worldAABB.lowerBound)
        lowerValues(0) = (quantizationFactor.x * vl.x).toInt & (Int.MaxValue - 1)
        upperValues(0) = (quantizationFactor.x * vu.x).toInt | 1

        lowerValues(1) = (quantizationFactor.y * vl.y).toInt & (Int.MaxValue - 1)
        upperValues(1) = (quantizationFactor.y * vu.y).toInt | 1
    }
  
    /**
     * @param results
     *            out variable
     */
    private def query(results: Array[Int], lowerValue: Int, upperValue: Int,
            bounds: Array[Bound], boundCount: Int, axis: Int) {
        if (BroadPhase.DebugPrint) {
            System.out.println("Query(6 args)");
        }

        val lowerQuery = BroadPhase.binarySearch(bounds, boundCount, lowerValue)
        val upperQuery = BroadPhase.binarySearch(bounds, boundCount, upperValue)

        // Easy case: lowerQuery <= lowerIndex(i) < upperQuery
        // Solution: search query range for min bounds.
        for (i <- lowerQuery until upperQuery) {
            if (bounds(i).isLower) {
                incrementOverlapCount(bounds(i).proxyId)
            }
        }
        // Hard case: lowerIndex(i) < lowerQuery < upperIndex(i)
        // Solution: use the stabbing count to search down the bound array.
        if (lowerQuery > 0) {
            var i = lowerQuery - 1
            var s = bounds(i).stabbingCount
            // Find the s overlaps.
            while (s != 0) {
                assert (i >= 0, "i = " + i + "; s = " + s)
                if (bounds(i).isLower) {
                    val proxy = proxyPool(bounds(i).proxyId)
                    if (lowerQuery <= proxy.upperBounds(axis)) {
                        incrementOverlapCount(bounds(i).proxyId)
                        s -= 1
                    }
                }
                i -= 1
            }
        }

        results(0) = lowerQuery
        results(1) = upperQuery
    }

    private def incrementOverlapCount(proxyId: Int) {
        if (BroadPhase.DebugPrint) {
            System.out.println("IncrementOverlapCount()");
        }

        val proxy = proxyPool(proxyId)
        if (proxy.timeStamp < timeStamp) {
            proxy.timeStamp = timeStamp
            proxy.overlapCount = 1;
        } else {
            proxy.overlapCount = 2
            assert(queryResultCount < Settings.maxProxies)
            queryResults(queryResultCount) = proxyId
            queryResultCount += 1
        }
    }

  private def incrementTimeStamp() {
    if (BroadPhase.DebugPrint) {
      System.out.println("IncrementTimeStamp()");
    }

    if (timeStamp == Int.MaxValue) {
      for (i <- 0 until Settings.maxProxies) {
        proxyPool(i).timeStamp = 0
      }
      timeStamp = 1
    } else {
      timeStamp += 1
    }
  }

  def inRange(aabb: AABB) = {
    val d = max(aabb.lowerBound - worldAABB.upperBound,
                worldAABB.lowerBound - aabb.upperBound)
    MathUtil.max(d.x, d.y) < 0.0f
  }

}
