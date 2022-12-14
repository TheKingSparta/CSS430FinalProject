/**
 * Worked on by: Renee
 * Purpose: The superblock the first block (BLOCK 0) in the disk.
 * The superblock stores information about the rest of the disk.
 * "For accessing this block, you should call SysLib.rawread( 0, data ) where data is a 512-byte array."
 * NO USER THREADS CAN ACCESS SUPER BLOCK
 *
 * Status: complete; freelist may cause issues
 */
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks; //number of disk blocks
    public int inodeBlocks; //number of inodes
    public int maxNumInodes; //holds the maximum number of inodes (inodeblocks * 16)
    //public int freeList; //block number of the free-list's head - points to the block number
    private int freeList;
    //false: block is not in use
    //true: block is in use

    // you implement
    public SuperBlock(int diskSize) {
        // read the superblock from disk
        byte[] data = new byte[512];
        SysLib.rawread(0, data);
        totalBlocks = SysLib.bytes2int(data, 0);//image this should be like sync function but not sure what all the parameters are for it
        inodeBlocks = SysLib.bytes2int(data, 4);
        freeList = SysLib.bytes2int(data, 8);

        if (totalBlocks == diskSize && inodeBlocks > 0 && freeList < 2) {
            return;
        } else {
            totalBlocks = diskSize;
            format(defaultInodeBlocks);
            SysLib.cerr("Superblock synchronized\n");
        }

        //Set each block at and after freeList to point to the next block
        byte[] buffer = new byte[512];
        for (int i = freeList; i < 999; i++) {
            SysLib.int2bytes(i + 1, buffer, 0);
            SysLib.rawwrite(i, buffer);
            buffer = new byte[512];
        }
        SysLib.int2bytes(0, buffer, 0);
        SysLib.rawwrite(999, buffer);
        SysLib.cerr("Superblock initialized\n");
    }

    //  helper function
    //converts all data to byte format to be written back to the disk
    void sync() {
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, superBlock, 0);
        SysLib.int2bytes(inodeBlocks, superBlock, 4);
        SysLib.int2bytes(freeList, superBlock, 8);
        maxNumInodes = inodeBlocks * 16;
        SysLib.rawwrite(0, superBlock);
    }

    //Formats the superblock
    void format() {
        // default format with 64 inodes
        format(defaultInodeBlocks);
    }

    //Formats the superblock
    void format(int files) {
        //For the default 64:
        //Block 0: super block
        //Block 1: inodes 0-15
        //Block 2: inodes 16-31
        //Block 3: inodes 32-47
        //Block 4: inodes 48-63
        //Block 5: first free block
        inodeBlocks = files / 16; //Number of blocks allocated for inodes
        totalBlocks = 1000; //Should always be 1000 :)
        freeList = inodeBlocks + 1;
        sync(); //write it onto the disk
        SysLib.cerr("Superblock has been formatted\n");
    }

    //Returns the next free block in the list
    public int getFreeBlock() {
        // get a new free block from the freelist
        int freeBlock = freeList;
        byte[] buffer = new byte[512];
        //Read in the next free block from the block that freeList points to and update freeList
        SysLib.rawread(freeBlock, buffer);
        freeList = SysLib.bytes2int(buffer, 0);
        sync();
        return freeBlock;
    }

    //Adds the input block into the freelist
    public boolean returnBlock(int oldBlockNumber) {
        // return this former block

        //Find end of list
        int currBlockNumber = freeList;
        byte[] buffer = new byte[512];
        SysLib.rawread(currBlockNumber, buffer);
        while (SysLib.bytes2int(buffer, 0) > 0) {
            currBlockNumber = SysLib.bytes2int(buffer, 0);
            SysLib.rawread(currBlockNumber, buffer);
        }

        //Set final block to point to oldBlockNumber
        SysLib.int2bytes(oldBlockNumber, buffer, 0);
        SysLib.rawwrite(currBlockNumber, buffer);

        //Set oldBlockNumber to 0
        SysLib.int2bytes(0, buffer, 0);
        SysLib.rawwrite(oldBlockNumber, buffer);
        sync();
        return true;
    }

}
