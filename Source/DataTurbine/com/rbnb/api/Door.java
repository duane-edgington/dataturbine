/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

/**
 * Door (synchronization) class.
 * <p>
 * This class provides special synchronization capabilities:
 * <p><ul>
 * <li>Multiple locks for a single thread without needing to stay within a
 *     particular method or section of code,</li>
 * <li>Multiple readers and a single writer (read/write lock).</li>
 * </ul><p>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.Lock
 * @since V2.0
 * @version 11/17/2003
 */

/*
 * Copyright 2001, 2002, 2003 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 08/06/2007  WHF	Added isDebug() static method.  Made id string optional.  
 * 11/17/2003  INB	Added <code>clear</code> method.
 *			<code>java.lang.InterruptedExceptions</code> always
 *			clear out any <code>Locks</code>.  Clear the thread out
 *			of <code>Locks</code> here rather than in
 *			<code>Lock.java</code>.
 * 11/14/2003  INB	Moved the <code>Lock</code> class out (it is no longer
 *			private).
 * 11/12/2003  INB	Added additional identification information so that
 *			messages about locks can assist in debugging lock ups
 *			better.
 * 10/28/2003  INB	Added <code>toString</code> method.
 * 10/22/2003  INB	Use <code>TimerPeriod.LOCK_WAIT</code>.
 * 10/17/2003  INB	No need to synchronize on find of read locks.  Also,
 *			when unlocking, only synchronize while we're removing
 *			the lock.
 * 07/30/2003  INB	Added <code>nullify</code> method.
 * 05/23/2003  INB	Ensure that we don't have cleared lock entries in
 *			the <code>readLocks</code> list while that list can
 *			be accessed.
 * 05/02/2003  INB	Call <code>SortedVector(1)</code> rather than
 *			<code>SortedVector()</code>.
 * 03/26/2003  INB	Use <code>TimerPeriod.NORMAL_WAIT</code>.
 * 02/21/2001  INB	Created.
 *
 */
final class Door {
    /**
     * identification for this <code>Door</code>.  Is only retained if
     *  isDebug() is true.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/12/2003
     */
    private String identification = null;

    /**
     * primary lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private Lock primaryLock = new Lock(this);

    /**
     * read locks.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */
    private com.rbnb.utility.SortedVector readLocks = null;

   /**
     * type of lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #READ_WRITE
     * @see #STANDARD
     * @since V2.0
     * @version 05/10/2001
     */
    private byte typeOfLock = STANDARD;

    // Packge constants:
    /**
     * read/write lock (any number of readers can share access, but a single
     * writer blocks all other access).
     * <p>
     *
     * @author Ian Brown
     *
     * @see #STANDARD
     * @since V2.0
     * @version 05/10/2001
     */
    final static byte READ_WRITE = 1;

    /**
     * standard type of lock (only one thread can have a lock at a time).
     * <p>
     *
     * @author Ian Brown
     *
     * @see #READ_WRITE
     * @since V2.0
     * @version 05/10/2001
     */
    final static byte STANDARD = 0;
    
    /**
      * Debug modes.
      * @author WHF
      * @version 2007/08/06
      */
    private static final int DEBUG_OFF = -1,
    	DEBUG_ON = 1,
	DEBUG_UNKNOWN = 0;
	
    /**
      * Current debug setting.
      * @author WHF
      * @version 2007/08/06
      */
    private static int debugMode = DEBUG_UNKNOWN;
    
    /**
      * String used for identification when debugging is off.
      * @author WHF
      * @version 2007/08/06
      */
    private static final String ID_DEBUG_OFF = "DEBUG OFF";
    

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    Door() {
	super();
    }

    /**
     * Class constructor to build a <code>Door</code> with the specified
     * type of lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeOfLockI  the type of lock on the <code>Door</code>.
     * @see #READ_WRITE
     * @see #STANDARD
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Call implementation that takes an identification.
     * 02/21/2001  INB	Created.
     *
     */
    Door(byte typeOfLockI)
	throws java.lang.InterruptedException
    {
	this("",typeOfLockI);
    }

    /**
     * Class constructor to build a <code>Door</code> with the specified
     * type of lock and identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeOfLockI  the type of lock on the <code>Door</code>.
     * @see #READ_WRITE
     * @see #STANDARD
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    Door(String identificationI,byte typeOfLockI)
	throws java.lang.InterruptedException
    {
	this();
	setIdentification(identificationI);
	setTypeOfLock(typeOfLockI);
    }

    /**
     * Completely clears a <code>Lock</code>.
     * <p>
     * This method is used to clean things up.
     * <p>
     *
     * @author Ian Brown
     *
     * @param lockI the <code>Lock</code> to be released.
     * @since V2.2
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Created.
     *
     */
    public final void clear(Lock lockI) {
	if (lockI == getPrimaryLock()) {
	    try {
		unlockReadWrite();
	    } catch (java.lang.Exception e) {
	    }

	} else {
	    if (getReadLocks().contains(lockI)) {
		synchronized (getReadLocks()) {
		    getReadLocks().removeElement(lockI);
		    getReadLocks().notifyAll();
		}
	    }
	    lockI.clear();
	}
    }

    /**
     * Gets the identification.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the identification.
     * @see #setIdentification(String)
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.n
     *
     */
    final String getIdentification() {
	return (identification);
    }

    /**
     * Gets the primary <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the primary <code>Lock</code>.
     * @see #setPrimaryLock(com.rbnb.api.Lock)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Made private to control variable scope.     
     * 02/21/2001  INB	Created.
     *
     */
    private final Lock getPrimaryLock() {
	return (primaryLock);
    }

    /**
     * Gets the read locks.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the read locks.
     * @see #setReadLocks(com.rbnb.utility.SortedVector)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Made private to control variable scope.     
     * 02/21/2001  INB	Created.
     *
     */
    private final com.rbnb.utility.SortedVector getReadLocks() {
	return (readLocks);
    }

    /**
     * Gets the type of lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the type of lock.
     * @see #READ_WRITE
     * #see #setTypeOfLock(byte)
     * @see #STANDARD
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/21/2001  INB	Created.
     *
     */
    final byte getTypeOfLock() {
	return (typeOfLock);
    }
    
    /**
      * True if debugging has been set for this class.  Based on the 
      * com.rbnb.sapi.Door.debug environment variable.
      *
      * @see System#getProperty(String)
      * @see Boolean#getBoolean(String)
      */
    static boolean isDebug() {
	if (debugMode == DEBUG_UNKNOWN) {
	    try {
		debugMode = Boolean.getBoolean("com.rbnb.api.Door.debug")
			? DEBUG_ON : DEBUG_OFF;
	    } catch (Throwable t) {
		debugMode = DEBUG_OFF;
	    }
	}
	return debugMode == DEBUG_ON;
    }
    

    /**
     * Determines if the primary lock on the door is set.
     * <p>
     *
     * @author Ian Brown
     *
     * @return is the primary lock set?
     * @exception java.lang.IllegalStateException
     *		  thrown if this is a READ/WRITE lock.
     * @see #lock()
     * @see #lock(String)
     * @see #unlock()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Eliminated <code>java.lang.InterruptedException</code>.
     * 11/12/2003  INB	Add more information to the exception.
     * 11/14/2002  INB	Created.
     *
     */
    final boolean isLocked() {
	boolean isLockedR = false;

	if (getTypeOfLock() == STANDARD) {
	    isLockedR = getPrimaryLock().check("",false,false);

	} else {
	    throw new java.lang.IllegalStateException
		(this + " cannot check for lock of READ/WRITE lock.");
	}

	return (isLockedR);
    }

    /**
     * Sets the primary <code>LocK</code> for this <code>Door</code>.
     * <p>
     * This method blocks until it is able to set the primary lock. The primary
     * lock can be set if:
     * <p><ul>
     * <li>No one else has set the primary lock, and</li>
     * <li>There are no active read locks.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @see #lockRead()
     * @see #lockRead(String)
     * @see #unlock()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Call version that takes a location.
     * 02/21/2001  INB	Created.
     *
     */
    final void lock()
	throws java.lang.InterruptedException
    {
	lock("");
    }

    /**
     * Sets the primary <code>LocK</code> for this <code>Door</code>.
     * <p>
     * This method blocks until it is able to set the primary lock. The primary
     * lock can be set if:
     * <p><ul>
     * <li>No one else has set the primary lock, and</li>
     * <li>There are no active read locks.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param locationI the location of the caller.
     * @param java.lang.InterruptedException
     *	      thrown if the operation is interrupted (no <code>Lock</code> is
     *	      set).
     * @see #lockRead()
     * @see #lockRead(String)
     * @see #unlock()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Created.
     *
     */
    final void lock(String locationI)
	throws java.lang.InterruptedException
    {
	if (getTypeOfLock() == STANDARD) {
	    getPrimaryLock().grab(locationI,false,false);
	} else {
	    lockReadWrite(locationI);
	}
    }

    /**
     * Sets a read lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lock()
     * @see #unlockRead()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/12/2003  INB	Calls the version that takes a location.
     * 10/17/2003  INB	No need to synchronize on find.
     * 05/23/2003  INB	Synchronize on the <code>readLocks</code> list when
     *			accessing it.
     * 02/21/2001  INB	Created.
     *
     */
    final void lockRead()
	throws java.lang.InterruptedException
    {
	lockRead("");
    }

    /**
     * Sets a read lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @param locationI the location of the caller.
     * @see #lock()
     * @see #unlockRead()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that no <code>Lock</code> is set on an
     *			<code>InterruptedException</code>.  Clear the thread on
     *			release of the primary <code>Lock</code>.
     * 11/12/2003  INB	Created.
     *
     */
    final void lockRead(String locationI)
	throws java.lang.InterruptedException
    {
	boolean grabbed = false;

	try {
	    try {
		// Look for an existing read lock.
		Lock readLock;

		readLock = (Lock)
		    getReadLocks().find(Thread.currentThread());

		if (readLock != null) {
		    // If there is a read lock already, just apply it again.
		    readLock.lock(locationI,true);

		} else {
		    // If one doesn't exist, create one.
		    readLock = new Lock(this);
		    readLock.lock(locationI,true);
		    // Grab the primary lock before adding the lock to the
		    // door. This ensures that there is no write lock active or
		    // waiting. Release the primary lock after the read lock is
		    // created.
		    grabbed = true;
		    try {
			getPrimaryLock().grab(locationI,true,false);
		    } catch (java.lang.InterruptedException e) {
			readLock.unlock();
			throw e;
		    }
		    synchronized (getReadLocks()) {
			getReadLocks().add(readLock);
		    }
		    releasePrimaryLock();
		    grabbed = false;		    
		    /*synchronized (getPrimaryLock()) {
			getPrimaryLock().release();
			if (getPrimaryLock().count == 0) {
			    getPrimaryLock().setThread(null);
			}
			grabbed = false;
		    }*/
		}

	    } catch (com.rbnb.utility.SortException e) {
		throw new java.lang.InternalError();
	    }

	} finally {
	    if (grabbed) {
		// Release the primary lock if we got it.
		releasePrimaryLock();
		/*synchronized (getPrimaryLock()) {
		    getPrimaryLock().release();
		    if (getPrimaryLock().count == 0) {
			getPrimaryLock().setThread(null);
		    }
		}*/

	    }
	}
    }

    /**
     * Sets the primary <code>LocK</code> for this <code>READ_WRITE
     * Door</code>.
     * <p>
     * This method blocks until it is able to set the primary lock. The primary
     * lock can be set if:
     * <p><ul>
     * <li>No one else has set the primary lock, and</li>
     * <li>There are no active read locks.</li>
     * </ul><p>
     *
     * @author Ian Brown
     *
     * @param locationI the location of the caller.
     * @see #lockRead()
     * @see #lockRead(String)
     * @see #unlock()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	<code>java.lang.InterruptedException</code> now simply
     *			releases all <code>Locks</code> set by this method and
     *			returns.  Clear the thread on release of the primary
     *			<code>Lock</code>.
     * 11/12/2003  INB	Added <code>locationI</code> parameter.
     * 10/22/2003  INB	Use <code>TimerPeriod.LOCK_WAIT</code>.
     * 10/17/2003  INB	No need to synchronized when doing find of read lock.
     * 05/23/2003  INB	Synchronize on <code>readLocks</code> list when
     *			accessing it.
     * 02/21/2001  INB	Created.
     *
     */
    private final void lockReadWrite(String locationI)
	throws java.lang.InterruptedException
    {
	boolean grabbed = false,
		isPending = false;
	try {
	    // Determine if we already have a read lock. If so, we can ignore
	    // the pending locks.
	    boolean checkPending = true;

	    try {
		if (getReadLocks() != null) {
		    checkPending =
			(getReadLocks().find
			 (Thread.currentThread()) == null);
		}
	    } catch (com.rbnb.utility.SortException e) {
		throw new InternalError();
	    }

	    // Now, set a pending lock.
	    getPrimaryLock().addPending(locationI);
	    isPending = true;

	    if (getReadLocks() != null) {
		// Wait for the read locks vector to have no entries that can
		// block us.

		long lastAt = System.currentTimeMillis();
		long nowAt;

		synchronized (getReadLocks()) {
		    boolean blocked;

		    do {
			blocked = false;
			for (int idx = 0; idx < getReadLocks().size(); ) {
			    // Loop through the read locks to ensure that they
			    // are still active.
			    Lock readLock = (Lock)
				getReadLocks().elementAt(idx);

			    // 2007/08/06  WHF  Refactoring; meaning same.	
			    if (/*readLock.getThread() ==*/
				readLock.threadEquals(
				Thread.currentThread())) {
				// It is legal to have both read and write
				// locks.
				++idx;

			    } else if (!readLock.check(locationI,
						       false,
						       false)) {
				// If a read lock isn't active (generally
				// because the thread is not active), remove it
				// from the list.
				getReadLocks().removeElementAt(idx);

			    } else {
				// If a read lock is still active, then we need
				// to wait.
				blocked = true;
				break;
			    }
			}

			if (blocked) {
			    // If we're blocked, wait for something to happen
			    // or a little while (the latter ensures that we
			    // don't get blocked by threads that die).
			    getReadLocks().wait(100);
			}

			if ((nowAt = System.currentTimeMillis()) - lastAt >=
			    TimerPeriod.LOCK_WAIT) {
			    try {
			    System.err.println("lockReadWrite blocked, clearing read locks and moving on!");
			    unlockRead();			// MJM clear locks and lumber on.  10/13
			    blocked=false;			// MJM actually lumber on... 6/15
			    /*
				throw new Exception
				    (System.currentTimeMillis() + " " +
				     this +
				     " lockReadWrite: blocked at " + locationI
				     + " waiting for read locks to " +
				     " clear for " +
				     Thread.currentThread());
				    */
			    } 
			    catch (Exception e) {
				e.printStackTrace();
				lastAt = nowAt;
			    }
			}

		    } while (blocked);
		}
	    }

	    // Grab the primary lock.
//	    getPrimaryLock().grab(locationI,checkPending,true);  // MJM 2/20/07
	    getPrimaryLock().grab(locationI,false,true);	// MJM: write locks shouldn't wait on pendingr?,
								// risks deadlock with read locks that do wait
	    grabbed = true;

	    // Set the primary lock.
	    getPrimaryLock().lock(locationI,true);

	} finally {
	    // Release the primary lock.
	    if (grabbed) {
		releasePrimaryLock();
		/*synchronized (getPrimaryLock()) {
		    getPrimaryLock().release();
		    if (getPrimaryLock().count == 0) {
			getPrimaryLock().setThread(null);
		    }
		}*/
	    }
	    // Release the pending count.
	    if (isPending) {
		getPrimaryLock().removePending();
	    }
	}
    }

    /**
     * Nullifies this <code>Door</code>.
     * <p>
     * This method ensures that all pointers
     * are cleared, reducing the effort needed by the garbage collector to
     * clean it up.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/12/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 07/30/2003  INB	Created.
     *
     */
    public final void nullify() {
	setIdentification(null);
	if (getReadLocks() != null) {
	    Lock lock;
	    while (getReadLocks().size() > 0) {
		lock = (Lock) getReadLocks().elementAt(0);
		getReadLocks().remove(lock);
		lock.nullify();
	    }
	    setReadLocks(null);
	}
    }

    /**
     * Sets the identification for this <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param identificationI the identification.
     * @see #getIdentification()
     * @since V2.2
     * @version 08/06/2007
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Only sets internal variable if isDebug() is true.
     * 11/12/2003  INB	Created.
     *
     */
    final void setIdentification(String identificationI) {
	if (isDebug())
	    identification = identificationI;
	else
	    identification = ID_DEBUG_OFF;
    }

    /**
     * Sets the primary <code>Lock</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param primaryLockI  the new primary <code>Lock</code>.
     * @see #getPrimaryLock()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Made private to control variable scope.
     * 02/21/2001  INB	Created.
     *
     */
    private final void setPrimaryLock(Lock primaryLockI) {
	primaryLock = primaryLockI;
    }

    /**
     * Sets the read locks.
     * <p>
     *
     * @author Ian Brown
     *
     * @param readLocksI  the read locks.
     * @see #getReadLocks()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/06/2007  WHF  Made private to control variable scope.     
     * 02/21/2001  INB	Created.
     *
     */
    private final void setReadLocks(com.rbnb.utility.SortedVector readLocksI) {
	readLocks = readLocksI;
    }

    /**
     * Sets the type of lock.
     * <p>
     *
     * @author Ian Brown
     *
     * @param typeOfLockI  the new type of lock.
     * @see #getTypeOfLock()
     * @see #READ_WRITE
     * @see #STANDARD
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/13/2007  WHF  Made private so only called from constructor.  In this
     *     way we know the type of lock cannot change midstream, simplifying
     *     multithreaded analysis.
     * 02/21/2001  INB	Created.
     *
     */
    private final void setTypeOfLock(byte typeOfLockI)
	throws java.lang.InterruptedException
    {
	lock();
	typeOfLock = typeOfLockI;
	if (getTypeOfLock() == STANDARD) {
	    setReadLocks(null);
	} else {
	    setReadLocks(new com.rbnb.utility.SortedVector());
	}
	unlock();
    }

    /**
     * Returns a string representation.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the string.
     * @since V2.2
     * @version 10/28/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/28/2003  INB	Created.
     *
     */
    public final String toString() {
	return ("Door:" +
		" Identification: " + getIdentification() +
		" Type: " + getTypeOfLock() +
		" Primary: " + getPrimaryLock() +
		" Read Locks: " + getReadLocks());
    }

    /**
     * Clears the primary <code>LocK</code> for this <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lock()
     * @see #unlockRead()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Clear the primary lock thread.
     * 02/21/2001  INB	Created.
     *
     */
    final void unlock()
	throws java.lang.InterruptedException
    {
	if (getTypeOfLock() == STANDARD) {
	    releasePrimaryLock();
	    /*synchronized (getPrimaryLock()) {
		getPrimaryLock().release();
		if (getPrimaryLock().count == 0) {
		    getPrimaryLock().setThread(null);
		}
	    }*/
	} else {
	    unlockReadWrite();
	}
    }

    /**
     * Clears a read lock.
     * <p>
     * The 
     *
     * @author Ian Brown
     *
     * @see #lockRead()
     * @see #unlock()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Eliminated <code>java.lang.InterruptedException</code>.
     *			Only synchronize when we're removing the
     *			<code>Lock</code>.
     * 05/23/2003  INB	Ensure that we don't have cleared lock entries in
     *			the <code>readLocks</code> list while that list can
     *			be accessed.
     * 02/21/2001  INB	Created.
     *
     */
    final void unlockRead() {
	// Look for an existing read lock.
	try {
	    Lock readLock = null;
	    for (int idx = 0; (readLock == null) && (idx < 10); ++idx) {
		readLock = (Lock)
		    getReadLocks().find(Thread.currentThread());

		if ((readLock != null) && readLock.unlock()) {
		    // If one exists, clear it. If the read lock is now off,
		    // remove it from the list.
		    synchronized (getReadLocks()) {
			getReadLocks().removeElement(readLock);
			getReadLocks().notifyAll();
		    }

		} else if (readLock == null) {
		    try {
			throw new Exception
			    ("Unlock failure:\n" +
			     this +
			     "\nFor: " +
			     Thread.currentThread() +
			     ((getReadLocks().size() > 0) ?
			      ("\nAttempt: " + idx + " of 10.") :
			      ""));
					     
		    } catch (Exception e) {
//			e.printStackTrace();				// MJM 6/15: unecessarily scary debug?
		    }

		    if (getReadLocks().size() == 0) {
			break;
		    }
		}
	    }

	} catch (com.rbnb.utility.SortException e) {
	    throw new java.lang.InternalError();
	}
    }


    /**
     * Clears the primary <code>LocK</code> for this <code>READ_WRITE
     * Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #lock()
     * @see #unlockRead()
     * @since V2.00
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/17/2003  INB	Ensure that <code>InteruptedExceptions</code> do not
     *			keep the <code>Lock</code> from being cleared.  Clear
     *			the <code>Thread</code> from the <code>Lock</code>.
     * 02/21/2001  INB	Created.
     *
     */
    private final void unlockReadWrite()
	throws java.lang.InterruptedException
    {
	java.lang.InterruptedException interruptedException = null;
	boolean grabbed = false;

	try {
	    // Grab the primary lock.
	    while (!grabbed) {
		try {
		    getPrimaryLock().grab("",false,false);
		    grabbed = true;
		} catch (java.lang.InterruptedException e) {
		    // Clear any interruptions and retry the grab.
		    interruptedException = e;
		    Thread.currentThread().interrupted();
		}
	    }

	    // Clear the primary lock.
	    getPrimaryLock().unlock();

	} finally {
	    // Release the primary lock.
	    if (grabbed) {
		releasePrimaryLock();
		/*synchronized (getPrimaryLock()) {
		    getPrimaryLock().release();
		    if (getPrimaryLock().count == 0) {
			getPrimaryLock().setThread(null);
		    }
		}*/
	    }

	    // Throw the <code>InterruptedException</code> if one happened.
	    if (interruptedException != null) {
		throw interruptedException;
	    }
	}
    }
    
    /**
     * Releases the primary lock.
     * <p>
     *
     * @author WHF
     *
     * @since V3.0
     * @version 2007/08/13
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 08/13/2007  WHF	Created.
     *
     */
    private final void releasePrimaryLock()
    {
	getPrimaryLock().release();
    }
}
