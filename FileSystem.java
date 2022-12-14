/**
 Worked on by: Zach and Renee
 Purpose:

 Status: complete
 */

public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem( int diskBlocks ) {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock( diskBlocks );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks * 16);
    
        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );
    
        // directory reconstruction
        FileTableEntry dirEnt = open( "/", "r" );
        int dirSize = fsize( dirEnt );
        if ( dirSize > 0 ) {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.bytes2directory( dirData );
        }
        close( dirEnt );
    }

    //Syncs to the superblock and directory.
    void sync( ) {
        // directory synchronization
        FileTableEntry dirEnt = open( "/", "w" );
        byte[] dirData = directory.directory2bytes( );
        write( dirEnt, dirData );
        close( dirEnt );
    
        // superblock synchronization
        superblock.sync( );
    }

    //Formats the superblock, directory, and filetable to store a max number of files equal to the input.
    //Deletes all existing files.
    boolean format( int files ) {
        //Delete all files
        // wait until all filetable entries are destructed
        while ( filetable.fempty( ) == false )
            filetable.freeAll();
    
        // format superblock, initialize inodes, and create a free list
        superblock.format( files );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks * 16);
    
        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );
    
        return true;
    }

    //Open a file, creating a new one if it doesn't already exist.
    FileTableEntry open( String filename, String mode ) {
        //No need to check if file exists, falloc() does it
        // filetable entry is allocated
        FileTableEntry newEntry = filetable.falloc(filename, mode);
        fsize(newEntry);
        //seek(newEntry, 0, SEEK_SET);
        return newEntry;
    }

    //Close an existing file, returns true if successful
    boolean close( FileTableEntry ftEnt ) {
        // filetable entry is freed
        synchronized ( ftEnt ) {
            // need to decrement count; also: changing > 1 to > 0 below
            ftEnt.count--;
            if ( ftEnt.count > 0 ) // my children or parent are(is) using it
                return true;
            return filetable.ffree( ftEnt );
        }
    }

    //Returns the filesize of the input FileTableEntry. Also updates the corresponding inode length.
    int fsize( FileTableEntry ftEnt ) {
        //Set the file size. The only way we know how is to read the file in.
        //Buffer should be large enough to store any file. This uses a ton of RAM though. I'm sorry.
        byte[] buffer = new byte[512 * ftEnt.inode.direct.length + 512 * 256];
        //Create a temporary copy to allow us to read the data, even if the original entry didn't support reading
        FileTableEntry temp = new FileTableEntry(ftEnt.inode, ftEnt.iNumber, "r");
        temp.seekPtr = 0;

        ftEnt.inode.length = read(temp, buffer);
        return ftEnt.inode.length;
    }

    //Reads the input FileTableEntry, copying the data to the buffer. Returns the number of bytes read.
    int read( FileTableEntry ftEnt, byte[] buffer ) {
        //Check if the mode allows the file to be read, return -1 if not
        if ( ftEnt.mode == "w" || ftEnt.mode == "a" ) {
            SysLib.cerr("Attempted to read without permission \n");
            return -1;
        }


        int offset   = 0;              // buffer offset
    
        synchronized ( ftEnt ) {
            //use seekpntr to get location to start reading from
            int startingDirectIndex = ftEnt.seekPtr / 512;

            byte[] blockData = new byte[512];
            //Read all the directs & get data from their blocks into buffer
            for(int i = startingDirectIndex; i < ftEnt.inode.direct.length; i++) {
                if(ftEnt.inode.direct[i] <= 0) {//we have hit end of file
                    return offset;
                }
                //read that block from the disk
                if(SysLib.rawread(ftEnt.inode.direct[i], blockData) == -1){
                    SysLib.cerr("RAW READ FAILED IN DIRECT READ() IN FILE SYSTEM");
                    return -1;
                }
                for(int j = ftEnt.seekPtr % 512; (j < 512) && (offset < buffer.length); j++) {
                    //add the block data to the buffer
                    buffer[offset] = blockData[j];
                    ftEnt.seekPtr++;
                    offset++;
                }
            }
            //Read the indirect
            byte[] indirectData = new byte[512];

            //If the indirect isn't set, return early. No need to check indirects
            if(ftEnt.inode.indirect <= 0) {
                return offset;
            }
            //read indirect data block
            if(SysLib.rawread(ftEnt.inode.indirect, indirectData) == -1){
                SysLib.cerr("RAW READ FAILED IN INDIRECT READ() IN FILE SYSTEM (1)");
                return -1;
            }
            int indirectOffset = 0;
            //start looping through indirect block by starting relative to where direct ended
            for(int i = startingDirectIndex - ftEnt.inode.direct.length; i < 256 && i >= 0; i++) {  //Stops @ 256: Each block has 512 bytes, each index is 2 bytes
                //Get the data from the block
                short nextBlock = SysLib.bytes2short(indirectData, indirectOffset);
                indirectOffset += 2;
                if(nextBlock <= 0) {//End of File
                    return offset;
                }
                if(SysLib.rawread(nextBlock, blockData) <= 0){//read block from disk
                    SysLib.cerr("RAW READ FAILED IN INDIRECT READ() IN FILE SYSTEM (2) \n");
                    SysLib.cerr("Next block: " + nextBlock + "\n");
                    return -1;
                }
                for(int j = ftEnt.seekPtr % 512; (j < 512) && (offset < buffer.length); j++) {//adding data from block to the buffer
                    buffer[offset] = blockData[j];
                    ftEnt.seekPtr++;
                    offset++;
                }
            }
        }
        return offset;
    }

    //Writes to the input FileTableEntry. Copies the data from buffer. Returns the number of bytes written.
    int write( FileTableEntry ftEnt, byte[] buffer ) {
        // at this point, ftEnt is only the one to modify the inode
        if ( ftEnt.mode == "r" )
            return -1;
    
        synchronized ( ftEnt ) {
            int offset   = 0;              // buffer offset
            int left     = buffer.length;  // the remaining data of this buffer

        }

        int currDirectIndex = ftEnt.inode.direct.length;
        int indirectIndex = 256;

        //Write to the indirects next
        //max 256 entries in the indirects

        byte[] indirects = new byte[512];

        //If the indirect isn't set, we won't need to read from it or return it
        if(ftEnt.inode.indirect > 0) {
            SysLib.rawread(ftEnt.inode.indirect, indirects);
        }


        if(ftEnt.mode == "w" || ftEnt.mode == "w+") {
            //If we're overwriting the file, start from the beginning
            seek(ftEnt, 0, SEEK_SET);
            //Return the blocks that are currently being used by the inode
            //Return the directs first
            for(int i = 0; i < ftEnt.inode.direct.length; i++) {
                if (ftEnt.inode.direct[i] > 0) {
                    superblock.returnBlock(ftEnt.inode.direct[i]);
                    ftEnt.inode.direct[i] = 0;
                }
            }
            currDirectIndex = 0;
            //Return the indirects
            for(int i = 0; i < 256; i++) {
                //Get the next indirect block and return it
                short nextBlock = SysLib.bytes2short(indirects, i * 2);
                if(nextBlock > 0)
                    superblock.returnBlock(nextBlock);

            }
            indirectIndex = 0;
            ftEnt.inode.direct[currDirectIndex] = (short) superblock.getFreeBlock();
        } else if(ftEnt.mode == "a") {
            //If we're appending to the file, start from the end
            seek(ftEnt, 0, SEEK_END);
            //If we're not starting past the directs
            if(ftEnt.seekPtr - (512 * ftEnt.inode.direct.length) < 0) {
                currDirectIndex = ftEnt.seekPtr / 512;
            } else { //If we ARE starting in the indirects
                indirectIndex = (ftEnt.seekPtr - (512 * ftEnt.inode.direct.length) ) / 512;
            }
        }

        int startingBlock = ftEnt.seekPtr / 512;
        int endPoint = ftEnt.seekPtr + buffer.length;


        //Where we're at in the input buffer
        int offset = 0;

        byte[] blockBuffer = new byte[512];

        //We might start partway through the first block, so we need to make sure we don't overwrite anything
        SysLib.rawread(startingBlock, blockBuffer);

        //Write to the directs first
        while((ftEnt.seekPtr <= endPoint) && currDirectIndex < ftEnt.inode.direct.length && offset < buffer.length){
            blockBuffer[ftEnt.seekPtr % 512] = buffer[offset];
            offset++;
            ftEnt.seekPtr++;
            //If we reach the end of a block, write the buffer to the block
            if(ftEnt.seekPtr % 512 == 0) {
                //write to disk
                if(SysLib.rawwrite(ftEnt.inode.direct[currDirectIndex], blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (1)");
                    return -1;
                }
                //Get a block for the next storage operation if there are more directs to use
                if(currDirectIndex < ftEnt.inode.direct.length - 1) {
                    currDirectIndex++;
                    ftEnt.inode.direct[currDirectIndex] = (short) superblock.getFreeBlock();
                }
            }
        }
        //write what we have to the disk
        if(currDirectIndex < ftEnt.inode.direct.length - 1) {
            currDirectIndex++;
            ftEnt.inode.direct[currDirectIndex] = (short) superblock.getFreeBlock();
            //write to disk
            if (SysLib.rawwrite(ftEnt.inode.direct[currDirectIndex - 1], blockBuffer) == -1) {
                SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (1)");
                return -1;
            }
        }

        //******************Start of indirects*************************//
        if((ftEnt.seekPtr <= endPoint) && (((ftEnt.seekPtr / 512) - ftEnt.inode.direct.length ) < 256) && offset < buffer.length)
            ftEnt.inode.indirect = (short) superblock.getFreeBlock();
        int indirectOffset = ftEnt.seekPtr / 512 - ftEnt.inode.direct.length;

        short block = 0;
        //byte[] bufferForBlockNumber = new byte[512];

        //Go until we run out of data to write, or until we run out of indirects
        while((ftEnt.seekPtr <= endPoint) && (((ftEnt.seekPtr / 512) - ftEnt.inode.direct.length ) < 256) && offset < buffer.length){
            blockBuffer[ftEnt.seekPtr % 512] = buffer[offset];
            offset++;
            ftEnt.seekPtr++;
            //If we reach the end of a block, write the buffer to the block
            if(ftEnt.seekPtr % 512 == 0) {
                indirectOffset += 2;
                //write to disk
                block = (short) superblock.getFreeBlock();
                SysLib.short2bytes(block, indirects, 0);    //TODO: Double Check offset
                SysLib.rawwrite(ftEnt.inode.indirect, indirects);
                if(SysLib.rawwrite(block, blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (2)");
                    return -1;
                }
                currDirectIndex++;
            }
        }
        //write anything else to the disk
        block = (short) superblock.getFreeBlock();
        SysLib.short2bytes(block, indirects, 2 * currDirectIndex);
        SysLib.rawwrite(ftEnt.inode.indirect, indirects);
        if(SysLib.rawwrite(block, blockBuffer) == -1) {
            SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (2)");
            return -1;
        }

        //Update the file size
        fsize(ftEnt);

        return offset;
    }

    //Set ftEnt's blocks to null
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        //make buffer array length of file
        int bufSize = ftEnt.inode.length;
        byte[] nullArray = new byte[bufSize];

        //make all elements in array null -  will be used to "dealocate" blocks
        for(byte b : nullArray){
            b = 0;
        }

        //set seek to the beginning of the file
        seek(ftEnt, 0, SEEK_SET);

        //use write to overwrite all data blocks to null
        if(write(ftEnt, nullArray) == -1){
            SysLib.cerr("ERROR IN DeallocAllBlocks in FileSystem: WRITE FAILED");
            return false;
        }
        return true;
    }

    //Delete the corresponding fileTableEntry and matching data.
    boolean delete( String filename ) {
        FileTableEntry ftEnt = open( filename, "w" );
        short iNumber = ftEnt.iNumber;
        return close( ftEnt ) && directory.ifree( iNumber );
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    //Set the seek pointer of the input FileTableEntry to the correct position, according to offset and whence
    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        synchronized ( ftEnt ) {
            int save_pos = ftEnt.seekPtr; //save seek pointer in case of offset error
            fsize(ftEnt);
            if(whence == SEEK_SET) {    //From the beginning
                ftEnt.seekPtr = offset;
            } else if (whence == SEEK_CUR) {    //From current pos
                ftEnt.seekPtr = ftEnt.seekPtr + offset;
            }
            else if (whence == SEEK_END) {  //From the end
                //Find the end of the file
                ftEnt.seekPtr = fsize(ftEnt) + offset;
            } else {
                SysLib.cerr("INVALID WHENCE IN seek(): " + whence);
            }

            if(ftEnt.seekPtr > fsize(ftEnt) || ftEnt.seekPtr < 0){
                SysLib.cerr("Incorrect Seek pointer position in seek \n");
                SysLib.cerr("Offset: " + offset + " Whence: " + whence + " Pos: " + ftEnt.seekPtr + "\n");
                ftEnt.seekPtr = save_pos;
                return -1;
            }
            return ftEnt.seekPtr;
		}

    }
}
