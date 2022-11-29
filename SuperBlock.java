
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks;
    public int inodeBlocks;
    public int freeList;
	
	// you implement
	public SuperBlock( int diskSize ) {
		// read the superblock from disk	
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
