/**
	Worked on by: Renee
	Purpose: The superblock the first block (BLOCK 0) in the disk.
	The superblock stores information about the rest of the disk.
	NO USER THREADS CAN ACCESS SUPER BLOCK

	Satus: incomplete
 */
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks; //number of disk blocks
    public int inodeBlocks; //number of inodes
    public int freeList; //block number of the free-list's head
	
	// you implement
	public SuperBlock( int diskSize ) {
		// read the superblock from disk
		totalBlocks = Disk.diskSize;
		inodeBlocks = Disk.inodeBlocks;
		freelist = Disk.freeList;
	}
	
	//  helper function
	void sync( ) {
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.int2bytes( totalBlocks, superBlock, 0 );
		SysLib.int2bytes( inodeBlocks, superBlock, 4 );
		SysLib.int2bytes( freeList, superBlock, 8 );
		SysLib.rawwrite( 0, superBlock );
		SysLib.cerr( "Superblock synchronized\n" );
    }

    void format( ) {
		// default format with 64 inodes
		format( defaultInodeBlocks );
    }
	
	// you implement
	 void format( int files ) {
		// initialize the superblock
		superBlock = new byte[Disk.blockSize];
	 }
	
	// you implement
	public int getFreeBlock( ) {
		// get a new free block from the freelist
	}
	
	// you implement
	public boolean returnBlock( int oldBlockNumber ) {
	// return this former block
	}
	
}
