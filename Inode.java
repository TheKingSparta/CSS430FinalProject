/**
Worked on by: Renee
	Purpose: Each inode represents one file, creates each inode and reads/writes it to the disk. The inodes are kept as
 refrences in the FileTableEntry.

	Satus: complete but not tested
 */
public class Inode {
	public final static int iNodeSize = 32;  // fixed to 32 bytes
    public final static int directSize = 11; // # direct pointers

    public final static int NoError              = 0;
    public final static int ErrorBlockRegistered = -1;
    public final static int ErrorPrecBlockUnused = -2;
    public final static int ErrorIndirectNull    = -3;

    public int length;                 // file size in bytes
    public short count;                // # file-table entries pointing to this
    public short flag;       // 0 = unused, 1 = used(r), 2 = used(!r), 
                             // 3=unused(wreg), 4=used(r,wreq), 5= used(!r,wreg)
    public short direct[] = new short[directSize]; // directo pointers
    public short indirect;                         // an indirect pointer

    Inode ( ) {                        // a default constructor
	length = 0;
	count = 0;
	flag = 1;
	for ( int i = 0; i < directSize; i++ )
	    direct[i] = 0;
	indirect = 0;
    }

	// making inode from disk
	Inode ( short iNumber ) {                  
		int blkNumber = 1 + iNumber / 16;          // inodes start from block#1
		byte[] data = new byte[Disk.blockSize]; 
		SysLib.rawread( blkNumber, data );         // get the inode block
		int offset = ( iNumber % 16 ) * iNodeSize; // locate the inode top

		length = SysLib.bytes2int( data, offset ); // retrieve all data members
		offset += 4;                               // from data
		count = SysLib.bytes2short( data, offset );
		offset += 2;
		flag = SysLib.bytes2short( data, offset );
		offset += 2;

		for ( int i = 0; i < directSize; i++ )
			direct[i] = 0;
		indirect = 0;

		for ( int i = 0; i < directSize; i++ ) {
			direct[i] = SysLib.bytes2short( data, offset );
			offset += 2;
		}
		indirect = SysLib.bytes2short( data, offset );
		offset += 2;

		/*
		System.out.println( "Inode[" + iNumber + "]: retrieved " +
					" length = " + length +
					" count = " + count +
					" flag = " + flag +
					" direct[0] = " + direct[0] +
					" indirect = " + indirect );
		*/
    }

 	
	// saving this inode to disk
	void toDisk( short iNumber ) {     
		// you implement
		//check corresponding inode on disk
		//if another thread updated it: read it from the disk and write the contents back

		int blkNumber = 1 + iNumber / 16;          // inodes start from block#1
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread( blkNumber, data );         // get the inode block
		int offset = ( iNumber % 16 ) * iNodeSize; // locate the inode top

		if((flag != 0) && (flag != 3)){
			//this inode (file) has been used by this or other threads
			//retrive this inode's data from the disk @ iNumber
			length = SysLib.bytes2int( data, offset ); // retrieve all data members
			offset += 4;                               // from data
			count = SysLib.bytes2short( data, offset );
			offset += 2;
			flag = SysLib.bytes2short( data, offset );
			offset += 2;
			for ( int i = 0; i < directSize; i++ ) {
				direct[i] = SysLib.bytes2short( data, offset );
				offset += 2;
			}
			indirect = SysLib.bytes2short( data, offset );
			offset += 2;
		}

		//write inode back to disk @ iNumber
		//put all inode attributes in data array
		offset = ( iNumber % 16 ) * iNodeSize; // reset offset to top of inode
		SysLib.int2bytes( length, data, offset ); // add all data member to the data[]
		offset += 4;
		SysLib.short2bytes( count, data, offset );
		offset += 2;
		SysLib.short2bytes( flag, data, offset );
		offset += 2;
		for ( int i = 0; i < directSize; i++ ) {
			SysLib.short2bytes( direct[i], data, offset );
			offset += 2;
		}
		SysLib.short2bytes( indirect, data, offset );
		offset += 2;
		SysLib.rawwrite(blkNumber, data);

	}
}
