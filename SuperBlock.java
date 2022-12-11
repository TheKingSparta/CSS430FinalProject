/**
	Worked on by: Renee
	Purpose: The superblock the first block (BLOCK 0) in the disk.
	The superblock stores information about the rest of the disk.
	"For accessing this block, you should call SysLib.rawread( 0, data ) where data is a 512-byte array."
	NO USER THREADS CAN ACCESS SUPER BLOCK

	Satus: complete; freelist may cause issues
 */
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks; //number of disk blocks
    public int inodeBlocks; //number of inodes
    //public int freeList; //block number of the free-list's head - points to the block number
	private boolean[] freeList_arr;
	//false: block is not in use
	//true: block is in use
	
	// you implement
	public SuperBlock( int diskSize ) {
		// read the superblock from disk
		byte[] data = new byte[512];
		SysLib.rawread(0, data);
		totalBlocks = SysLib.bytes2int(data, 0);//image this should be like sync function but not sure what all the parameters are for it
		inodeBlocks = SysLib.bytes2int(data, 4);
		//freeList = SysLib.bytes2int(data, 8);
		freeList_arr = new boolean[totalBlocks + 1];
		int offset = 4;
		for(boolean block : freeList_arr){
			block =  data[offset + 1] != 0;
			//superBlock[offset + 1] = (byte) (block ? 1 : 0);
			offset++;
		}

		if(totalBlocks == diskSize && inodeBlocks > 0 &&  freeList_arr[0] && freeList_arr[1]) {
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

		int offset = 4;
		for(boolean block : freeList_arr){
			superBlock[offset + 1] = (byte) (block ? 1 : 0);
			offset++;
		}
		//SysLib.int2bytes( freeList, superBlock, 8 );
		SysLib.rawwrite( 0, superBlock );
		SysLib.cerr( "Superblock synchronized\n" );
    }

    void format( ) {
		// default format with 64 inodes
		format( defaultInodeBlocks );
    }
	
	//TODO: should blocks 1-3 be set???
	 void format( int files ) {
		//Block 0: super block
		//Block 1: inodes 0-21
		//Block 2: inodes 22-43
		//Block 3: inodes 44-63
		//Block 4: first free block
		inodeBlocks = files; //number of inodes????
		totalBlocks = files / 16; //each block will have 16 inodes
		//freeList = totalBlocks + 1;
		freeList_arr = new boolean[totalBlocks + 1];//keep track of blocks in use
		for(boolean block : freeList_arr){
			block = false;//set all blocks to not in use
		}
		freeList_arr[0] = true;//set super block to be in use
		sync(); //write it onto the disk
		SysLib.cerr("Superblock has been formated");
	 }
	
	//TODO: you implement
	public int getFreeBlock( ) {
		// get a new free block from the freelist
		for(int i = 1; i < freeList_arr.length; i++){
			if(freeList_arr[i] == false){
				return i;
			}
		}
		return -1; //If there is no free block
	}
	
	//TODO: you implement
	//based on how its used what it does? clear this block? add it to free list?
	public boolean returnBlock( int oldBlockNumber ) {
		// return this former block
		if(oldBlockNumber != 0){
			freeList_arr[oldBlockNumber] = false;
			return true;
		}
		return false;
	}
	
}
