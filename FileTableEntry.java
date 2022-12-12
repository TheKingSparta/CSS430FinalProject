/**
 Worked on by: Zach
 Purpose: Each file table entry contains a seek pointer and inode number of the file it refrences. The file table
 entries are kept in the FileTable.table vector. Each user thread will have an file table entry.

 Satus: complete but not tested
 */
public class FileTableEntry {
	public int seekPtr;        //    a file seek pointer
    public final Inode inode;  //    a reference to an inode
    public final short iNumber;//    this inode number
    public int count;          //    a count to maintain #threads sharing this
    public final String mode;  //    "r", "w", "w+", or "a"
    FileTableEntry ( Inode i, short inumber, String m ) {
	seekPtr = 0;           // the seek pointer is set to the file top.
	inode = i;
        iNumber = inumber;     
        count = 1;           // at least one thread is using this entry.
        mode = m;            // once file access mode is set, it never changes.

	if ( mode.compareTo( "a" ) == 0 )
	    seekPtr = inode.length;
    }
}
