import java.util.*;

public class Directory {
	private static int maxChars = 30; // the max characters of each file name

    private int fsizes[];             // the actual size of each file name
    private char fnames[][];          // file names in characters

    public Directory ( int maxInumber ) {       // a default constructor
	fsizes = new int[maxInumber];           // maxInumber = max files
	for ( int i = 0; i < maxInumber; i++ )  // all file sizes set to 0
	    fsizes[i] = 0;
	fnames = new char[maxInumber][maxChars];

	String root = "/";                      // entry(inode) 0 is "/"
	fsizes[0] = root.length( );
	root.getChars( 0, fsizes[0], fnames[0], 0 ); 
    }
	
	// you implement
	public void bytes2directory( byte data[] ) {
		// assumes data[] contains directory information retrieved from disk
        // initialize the directory fsizes[] and fnames[] with this data[]
	}
	
	public byte[] directory2bytes( ) {
        // converts and return directory information into a plain byte array
		// this byte array will be written back to disk
		byte[] data = new byte[fsizes.length * 4 + fnames.length * maxChars * 2];
		int offset = 0;
		for ( int i = 0; i < fsizes.length; i++, offset += 4 )
			SysLib.int2bytes( fsizes[i], data, offset );

		for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2 ) {
			String tableEntry = new String( fnames[i], 0, fsizes[i] );
			byte[] bytes = tableEntry.getBytes( );
			System.arraycopy( bytes, 0, data, offset, bytes.length );
		}
		return data;
		}

		public short ialloc ( String filename ) {
		// filename is the name of a file to be created.
		// allocates a new inode number for this filename.
		short i;
		// i = 0 is already used for "/"
		for ( i = 1; i < fsizes.length; i++ ) {
			if ( fsizes[i] == 0 ) {
			fsizes[i] = Math.min( filename.length( ), maxChars );
			filename.getChars( 0, fsizes[i], fnames[i], 0 );
			return i;
			}
		}
		return -1;			
    }

	// you implement
    public boolean ifree ( short iNumber ) {
		// deallocates this inumber (inode number).
		// the corresponding file will be deleted.
    }

    public short namei( String filename ) {
		// returns the inumber corresponding to this filename
		short i;
		for ( i = 0; i < fsizes.length; i++ ) {
			if ( fsizes[i] == filename.length( ) ) {
			String tableEntry = new String( fnames[i], 0, fsizes[i] );
			if ( filename.compareTo( tableEntry ) == 0 )
				return i;
			}
		}
		return -1;
    }
	
	
}
