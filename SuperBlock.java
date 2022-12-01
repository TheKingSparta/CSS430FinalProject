/**
	Worked on by: Renee
	Purpose: The superblock the first block (BLOCK 0) in the disk.
	The superblock stores information about the rest of the disk.
	"For accessing this block, you should call SysLib.rawread( 0, data ) where data is a 512-byte array."
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
		byte[] data = new byte[512];
		SysLib.rawread(0, data);
		SysLib.bytes2int(data, totalBlocks, 0);//image this should be like sync function but not sure what all the parameters are for it
		SysLib.bytes2int(data, inodeBlocks, 4);
		SysLib.bytes2int(data, freeList, 8);
		SysLib.cerr("Suberblock intialized");
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
