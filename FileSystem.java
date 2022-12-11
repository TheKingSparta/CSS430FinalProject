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
            }
             */

            //TODO: Seek pntrs :

            //TODO: Check buffer is large enough

            //TODO: Check rawread() returns

            byte[] blockData = new byte[512];
            //Read all the directs
            for(int i = 0; i < ftEnt.inode.direct.length; i++) {
                //Get the data from the block
                if(ftEnt.inode.direct[i] == -1) {
                    return offset;
                }
                SysLib.rawread(ftEnt.inode.direct[i], blockData);
                for(int j = 0; j < 512; j++) {
                    buffer[offset + j] = blockData[j];
                }
                offset += 512;
            }
            //Read the indirect
            byte[] indirectData = new byte[512];
            SysLib.rawread(ftEnt.inode.indirect, indirectData);
            int indirectOffset = 0;
            for(int i = 0; i < 256; i++) {
                //Get the data from the block
                short nextBlock = SysLib.bytes2short(indirectData, indirectOffset);
                indirectOffset += 2;
                if(nextBlock == -1) {
                    return offset;
                }
                SysLib.rawread(nextBlock, blockData);
                for(int j = 0; j < 512; j++) {
                    buffer[offset + j] = blockData[j];
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
    }

    //TODO: deallocAllBlocks
    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {

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
            /*
            System.out.println( "seek: offset=" + offset +
                    " fsize=" + fsize( ftEnt ) +
                    " seekptr=" + ftEnt.seekPtr +
                    " whence=" + whence );
            */
			
		}

    }
}
