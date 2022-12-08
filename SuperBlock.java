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
    public int freeList; //block number of the free-list's head - points to the block number
	
	// you implement
	public SuperBlock( int diskSize ) {
		// read the superblock from disk
		byte[] data = new byte[512];
		SysLib.rawread(0, data);
		totalBlocks = SysLib.bytes2int(data, 0);//image this should be like sync function but not sure what all the parameters are for it
		inodeBlocks = SysLib.bytes2int(data, 4);
		freeList = SysLib.bytes2int(data, 8);

		if(totalBlocks == diskSize && inodeBlocks > 0 && freeList >= 2) {
			return;
		} else {
			totalBlocks = diskSize;
			format(defaultInodeBlocks);
		}

		SysLib.cerr("Superblock initialized");
	}
	
	//  helper function
	//converts all data to byte format to be written back to the disk
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
	
	//TODO: you implement
	 void format( int files ) {
		//Block 0: super block
		//Block 1: inodes 0-21
		//Block 2: inodes 22-43
		//Block 3: inodes 44-63
		//Block 4: first free block
		inodeBlocks = 3;//number of inodes????
		freelist = 4;
		totalBlocks = 64;
		sync(); //write it onto the disk
		SysLib.cerr("Superblock has been formated");
	 }
	
	//TODO: you implement
	public int getFreeBlock( ) {
		// get a new free block from the freelist
		freelist_return = freelist;
		freelist++;
		return freelist_return;
	}
	
	//TODO: you implement
	public boolean returnBlock( int oldBlockNumber ) {
		// return this former block
		byte[] data = new byte[512];
		SysLib.rawread(oldBlockNumber, data);
		
	}
	
}
