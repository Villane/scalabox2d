package org.villane.box2d.collision.broadphase

import collection.jcl.HashMap

object PairManager {
  val NullPair = Int.MaxValue
  val NullProxy = Int.MaxValue
  val TableCapacity = Settings.maxPairs // must be a power of 2
  val TableMask = TableCapacity - 1
}

/**
 * ERKKI: This had a custom HashTable impl, but I removed it for a standard one
 * 
 * Needs a bit of cleanup. It should be possible to create less tuples.
 */
class PairManager(val broadPhase: BroadPhase, val callback: PairListener) {
  var hashTable = new HashMap[(Int,Int),Pair]()
  var pairCount = 0
  val pairBuffer = new Array[(Int,Int)](Settings.maxPairs)
  var pairBufferCount = 0

  // Add a pair and return the new pair. If the pair already exists,
  // no new pair is created and the old one is returned.
  def addPair(proxyId1: Int, proxyId2: Int): Pair = {
    // System.out.printf("PairManager.Add(%d, %d)\n", proxyId1, proxyId2);
    val proxies = if (proxyId1 > proxyId2) (proxyId2, proxyId1) else (proxyId1, proxyId2)

    hashTable.get(proxies) match {
      case Some(pair) => pair
      case None =>
        assert(pairCount < Settings.maxPairs, "Too many pairs (shape AABB overlaps) - this usually means you have too many bodies, or you need to increase Settings.maxPairs.")
        val pair = Pair(proxies._1, proxies._2)
        hashTable += proxies -> pair
        pairCount += 1
        pair
    }
  }
    
  // Remove a pair, return the pair's userData.
  def removePair(proxyId1: Int, proxyId2: Int): AnyRef = {
    assert(pairCount > 0)
    val proxies = if (proxyId1 > proxyId2) (proxyId2, proxyId1) else (proxyId1, proxyId2)

    hashTable.removeKey(proxies) match {
      case Some(pair) =>
        pairCount -= 1
        pair.userData
      case None =>
        assert(false, "Attempted to remove a pair that does not exist")
    }
  }
    
    /*
    As proxies are created and moved, many pairs are created and destroyed. Even worse, the same
    pair may be added and removed multiple times in a single time step of the physics engine. To reduce
    traffic in the pair manager, we try to avoid destroying pairs in the pair manager until the
    end of the physics step. This is done by buffering all the RemovePair requests. AddPair
    requests are processed immediately because we need the hash table entry for quick lookup.

    All user user callbacks are delayed until the buffered pairs are confirmed in Commit.
    This is very important because the user callbacks may be very expensive and client logic
    may be harmed if pairs are added and removed within the same time step.

    Buffer a pair for addition.
    We may add a pair that is not in the pair manager or pair buffer.
    We may add a pair that is already in the pair manager and pair buffer.
    If the added pair is not a new pair, then it must be in the pair buffer (because RemovePair was called).
    */
    def addBufferedPair(id1: Int, id2: Int) = {
        assert(id1 != PairManager.NullProxy && id2 != PairManager.NullProxy)
        assert(pairBufferCount < Settings.maxPairs)

        val pair = addPair(id1, id2)

        // If this pair is not in the pair buffer ...
        if (!pair.isBuffered) {
            // This must be a newly added pair.
            assert(!pair.isFinal)

            // Add it to the pair buffer.
            pair.setBuffered()
            pairBuffer(pairBufferCount) = if (id1 > id2) (id2, id1) else (id1, id2)
            pairBufferCount += 1

            assert(pairBufferCount <= pairCount)
        }

        // Confirm this pair for the subsequent call to Commit.
        pair.clearRemoved()

        if (BroadPhase.Validate) {
            validateBuffer()
        }
    }
    
    // Buffer a pair for removal.
    def removeBufferedPair(id1: Int, id2: Int) {
        assert(id1 != PairManager.NullProxy && id2 != PairManager.NullProxy)
        assert(pairBufferCount < Settings.maxPairs)
        val proxies = if (id1 > id2) (id2, id1) else (id1, id2)

        hashTable.get(proxies) match {
          case Some(pair) =>
            // If this pair is not in the pair buffer ...
            if (!pair.isBuffered) {
              // This must be an old pair.
              assert(pair.isFinal)

              pair.setBuffered()
              pairBuffer(pairBufferCount) = proxies
              pairBufferCount += 1

              assert(pairBufferCount <= pairCount)
            }

            pair.setRemoved()

            if (BroadPhase.Validate) {
              validateBuffer();
            }
          case None =>
            // The pair never existed. This is legal (due to collision filtering).
        }
    }

    def commit() {
        //System.out.println("Entering commit");
        var removeCount = 0

        val proxies = broadPhase.proxyPool

        for (i <- 0 until pairBufferCount) {
            val pair = hashTable(pairBuffer(i))
            assert(pair.isBuffered)
            pair.clearBuffered()

            assert(pair.proxyId1 < Settings.maxProxies && pair.proxyId2 < Settings.maxProxies)

            val proxy1 = proxies(pair.proxyId1)
            val proxy2 = proxies(pair.proxyId2)

            assert(proxy1.isValid)
            assert(proxy2.isValid)

            if (pair.isRemoved) {
                // It is possible a pair was added then removed before a commit. Therefore,
                // we should be careful not to tell the user the pair was removed when the
                // the user didn't receive a matching add.
                if (pair.isFinal) {
                    callback.pairRemoved(proxy1.userData, proxy2.userData, pair.userData)
                }

                // Store the ids so we can actually remove the pair below.
                pairBuffer(removeCount) = pairBuffer(i)
                //System.out.println("Buffering "+pair.proxyId1 + ", "+pair.proxyId2 + " for removal");
                removeCount += 1
            } else {
                assert(broadPhase.testOverlap(proxy1, proxy2))

                if (!pair.isFinal) {
                    pair.userData = callback.pairAdded(proxy1.userData, proxy2.userData)
                    pair.setFinal()
                }
            }
        }
        
        for (i <- 0 until removeCount) {
          removePair(pairBuffer(i)._1, pairBuffer(i)._2)
//            System.out.println("Remaining pairs: ");
//            for (int j=0; j<m_pairCount; ++j) {
//                System.out.println("  "+m_pairs[j].proxyId1 + ", " + m_pairs[j].proxyId2);
//            }
        }

        pairBufferCount = 0

        if (BroadPhase.Validate) {
            validateTable()
        }
    }
    
    /**
     * Unimplemented - for debugging purposes only in C++ version
     */
    def validateBuffer() {
    //#ifdef _DEBUG
//        assert(m_pairBufferCount <= m_pairCount);
//
//        std::sort(m_pairBuffer, m_pairBuffer + m_pairBufferCount);
//
//        for (int32 i = 0; i < m_pairBufferCount; ++i)
//        {
//            if (i > 0)
//            {
//                b2Assert(Equals(m_pairBuffer[i], m_pairBuffer[i-1]) == false);
//            }
//
//            b2Pair* pair = Find(m_pairBuffer[i].proxyId1, m_pairBuffer[i].proxyId2);
//            b2Assert(pair->IsBuffered());
//
//            b2Assert(pair->proxyId1 != pair->proxyId2);
//            b2Assert(pair->proxyId1 < b2_maxProxies);
//            b2Assert(pair->proxyId2 < b2_maxProxies);
//
//            b2Proxy* proxy1 = m_broadPhase->m_proxyPool + pair->proxyId1;
//            b2Proxy* proxy2 = m_broadPhase->m_proxyPool + pair->proxyId2;
//
//            b2Assert(proxy1->IsValid() == true);
//            b2Assert(proxy2->IsValid() == true);
//        }
    //#endif
    }

    /**
     * For debugging
     */
    def validateTable() {
//    #ifdef _DEBUG
        /*for (int i = 0; i < TABLE_CAPACITY; ++i) {
            int index = m_hashTable[i];
            while (index != NULL_PAIR) {
                Pair pair = m_pairs[index];
                assert(pair.isBuffered() == false);
                assert(pair.isFinal() == true);
                assert(pair.isRemoved() == false);

                assert(pair.proxyId1 != pair.proxyId2);
                assert(pair.proxyId1 < Settings.maxProxies);
                assert(pair.proxyId2 < Settings.maxProxies);

                Proxy proxy1 = m_broadPhase.m_proxyPool[pair.proxyId1];
                Proxy proxy2 = m_broadPhase.m_proxyPool[pair.proxyId2];

                assert(proxy1.isValid() == true);
                assert(proxy2.isValid() == true);

                assert(m_broadPhase.testOverlap(proxy1, proxy2) == true);

                index = pair.next;
            }
        }*/
//    #endif
    }
    
    def hash(proxyId1: Int, proxyId2: Int) = {
        var key = (proxyId2 << 16) | proxyId1
        key = ~key + (key << 15);
        key = key ^ (key >>> 12);
        key = key + (key << 2);
        key = key ^ (key >>> 4);
        key = key * 2057;
        key = key ^ (key >>> 16);
        key
    }


}
