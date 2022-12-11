public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem( int diskBlocks ) {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock( diskBlocks );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks );
    
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

    void sync( ) {
        // directory synchronizatioin
        FileTableEntry dirEnt = open( "/", "w" );
        byte[] dirData = directory.directory2bytes( );
        write( dirEnt, dirData );
        close( dirEnt );
    
        // superblock synchronization
        superblock.sync( );
    }


    //TODO: Should be done
    boolean format( int files ) {
        // wait until all filetable entries are destructed
        while ( filetable.fempty( ) == false )
            ;
    
        // format superblock, initialize inodes, and create a free list
        superblock.format( files );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks );
    
        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );
    
        return true;
    }

    //TODO: Should be done
    FileTableEntry open( String filename, String mode ) {
        //No need to check if file exists, falloc() does it
        // filetable entry is allocated
        return filetable.falloc(filename, mode);
    }

    boolean close( FileTableEntry ftEnt ) {
        // filetable entry is freed
        synchronized ( ftEnt ) {
            // need to decrement count; also: changing > 1 to > 0 below
            ftEnt.count--;
            if ( ftEnt.count > 0 ) // my children or parent are(is) using it
                return true;
        }
        return filetable.ffree( ftEnt );
    }

    //TODO: Should be done
    int fsize( FileTableEntry ftEnt ) {
        return ftEnt.inode.length;
    }

    //TODO: read()
    int read( FileTableEntry ftEnt, byte[] buffer ) {
        //Check if the mode allows the file to be read, return -1 if not
        if ( ftEnt.mode == "w" || ftEnt.mode == "a" )
            return -1;


        int offset   = 0;              // buffer offset
        int left     = buffer.length;  // the remaining data of this buffer
    
        synchronized ( ftEnt ) {
			// repeat reading until no more data  or reaching EOF
            //Each direct is a short, a short is 2 bytes
            /*
            for(int i = 0; i < ftEnt.inode.direct.length && i < left / 2; i++) {
                SysLib.short2bytes(ftEnt.inode.direct[i], buffer, offset);
                offset += 2;
            } OLD CODE
             */

            //use seekpntr to get location to start reading from
            int startingPoint = ftEnt.seekPtr / 512;

            byte[] blockData = new byte[512];
            //Read all the directs & get data from their blocks into buffer
            for(int i = startingPoint; i < ftEnt.inode.direct.length; i++) {
                if(ftEnt.inode.direct[i] == -1) {//we have hit end of file
                    return offset;
                }
                //read that block from the disk
                if(SysLib.rawread(ftEnt.inode.direct[i], blockData) == -1){
                    SysLib.cerr("RAW READ FAILED IN DIRECT READ() IN FILE SYSTEM");
                    return -1;
                }
                for(int j = ftEnt.seekPtr % 512; (j < 512) && ((j+offset) < buffer.length); j++) {
                    //add the block data to the buffer
                    buffer[offset + j] = blockData[j];
                    ftEnt.seekPtr++;
                }
                offset += 512;
            }
            //Read the indirect
            byte[] indirectData = new byte[512];

            //read indirect data block
            if(SysLib.rawread(ftEnt.inode.indirect, indirectData) == -1){
                SysLib.cerr("RAW READ FAILED IN INDIRECT READ() IN FILE SYSTEM (1)");
                return -1;
            }
            int indirectOffset = 0;
            //start looping through indirect block by starting relative to where direct ended
            for(int i = startingPoint - ftEnt.inode.direct.length; i < 256; i++) {  //Stops @ 256: Each block has 512 bytes, each index is 2 bytes
                //Get the data from the block
                short nextBlock = SysLib.bytes2short(indirectData, indirectOffset);
                indirectOffset += 2;
                if(nextBlock == -1) {//End of File
                    return offset;
                }
                if(SysLib.rawread(nextBlock, blockData) == -1){//read block from disk
                    SysLib.cerr("RAW READ FAILED IN INDIRECT READ() IN FILE SYSTEM (2)");
                    return -1;
                }
                for(int j = ftEnt.seekPtr % 512; (j < 512) && ((j + offset) < buffer.length); j++) {//adding data from block to the buffer
                    buffer[offset + j] = blockData[j];
                    ftEnt.seekPtr++;
                }
                offset += 512;
            }
        }
        return offset;
    }

    //TODO: finish write() ?
    int write( FileTableEntry ftEnt, byte[] buffer ) {
        // at this point, ftEnt is only the one to modify the inode
        if ( ftEnt.mode == "r" )
            return -1;
    
        synchronized ( ftEnt ) {
            int offset   = 0;              // buffer offset
            int left     = buffer.length;  // the remaining data of this buffer

        }

        //find end of file
        //int cur_pos = ftEnt.seekPtr;//save current position
        //seek(ftEnt,0, SEEK_END);
        //if(ftEnt.seekPtr == -1){
        //    SysLib.cerr("ERROR 1 IN WRITE IN FileSystem, call the police");
        //}

        //Make sure we're starting past the end of the data
        //ftEnt.seekPtr++;

        //write from seekPtr

        //Starting point is the block seekptr points to at start
        int startingBlock = ftEnt.seekPtr / 512;
        int endPoint = ftEnt.seekPtr + buffer.length;

        //Where we're at in the input buffer
        int offset = 0;

        byte[] blockBuffer = new byte[512];

        //We might start partway through the first block, so we need to make sure we don't overwrite anything
        SysLib.rawread(startingBlock, blockBuffer);

        //Write to the directs first
        while((ftEnt.seekPtr <= endPoint) && ((ftEnt.seekPtr / 512) < ftEnt.inode.direct.length)){
            blockBuffer[ftEnt.seekPtr % 512] = buffer[offset];
            offset++;
            ftEnt.seekPtr++;
            //If we reach the end of a block, write the buffer to the block
            if(ftEnt.seekPtr % 512 == 0) {
                //write to disk
                if(SysLib.rawwrite(ftEnt.seekPtr / 512 - 1, blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (1)");
                    return -1;
                }
                //read the next block from disk to ensure we don't overwrite anything if we stop partway through
                if(SysLib.rawread(ftEnt.seekPtr / 512, blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWREAD IN WRITE() IN FILESYSTEM (1)");
                    return -1;
                }
            }
        }
        //Write to the indirects next
        //max 256 entries in the indirects
        byte[] indirects = new byte[512];
        int indirectOffset = ftEnt.seekPtr / 512 - ftEnt.inode.direct.length;
        SysLib.rawread(ftEnt.inode.indirect, indirects);
        //Go until we run out of data to write, or until we run out of indirects
        while((ftEnt.seekPtr <= endPoint) && (((ftEnt.seekPtr / 512) - ftEnt.inode.direct.length ) < 256)){
            blockBuffer[ftEnt.seekPtr % 512] = buffer[offset];
            offset++;
            ftEnt.seekPtr++;
            //If we reach the end of a block, write the buffer to the block
            if(ftEnt.seekPtr % 512 == 0) {
                indirectOffset += 2;
                //write to disk
                short block = SysLib.bytes2short(indirects, indirectOffset - 2);
                if(SysLib.rawwrite(block, blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWWRITE IN WRITE() IN FILESYSTEM (2)");
                    return -1;
                }
                block = SysLib.bytes2short(indirects, indirectOffset);
                //read the next block from disk to ensure we don't overwrite anything if we stop partway through
                if(SysLib.rawread(block, blockBuffer) == -1) {
                    SysLib.cerr("ERROR ON RAWREAD IN WRITE() IN FILESYSTEM (2)");
                    return -1;
                }
            }
        }

        //Update the file size
        ftEnt.inode.length = ftEnt.inode.length + offset;
        return offset;
    }

    //Set ftEnt's blocks to null
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        //make buffer array length of file
        int bufSize = ftEnt.inode.length;
        byte[] nullArray = new byte[bufSize];

        //make all elements in array null -  will be used to "dealocate" blocks
        for(byte b : nullArray){
            b = null;
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

	
	
	
    boolean delete( String filename ) {
        FileTableEntry ftEnt = open( filename, "w" );
        short iNumber = ftEnt.iNumber;
        return close( ftEnt ) && directory.ifree( iNumber );
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    //TODO: seek()
    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        synchronized ( ftEnt ) {
            int save_pos = ftEnt.seekPtr; //save seek pointer in case of offset error
            /*
            System.out.println( "seek: offset=" + offset +
                    " fsize=" + fsize( ftEnt ) +
                    " seekptr=" + ftEnt.seekPtr +
                    " whence=" + whence );
            */
            if(whence == SEEK_SET) {    //From the beginning
                ftEnt.seekPtr = offset;
            } else if (whence == SEEK_CUR) {    //From current pos
                ftEnt.seekPtr = ftEnt.seekPtr + offset;
            }
            else if (whence == SEEK_END) {  //From the end
                //Find the end of the file
                ftEnt.seekPtr = ftEnt.inode.length + offset;
            } else {
                SysLib.cerr("INVALID WHENCE IN seek(): " + whence);
            }

            if(ftEnt.seekPtr > ftEnt.inode.length || ftEnt.seekPtr < 0){
                SysLib.cerr("RAW READ FAILED IN INDIRECT READ() IN FILE SYSTEM (2)");
                ftEnt.seekPtr = save_pos;
                return -1;
            }
            return ftEnt.seekPtr;
		}

    }
}
