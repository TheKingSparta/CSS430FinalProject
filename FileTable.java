import java.util.Vector;

/**
 * Worked on by: Zach
 * Purpose: The file table is shared by all user threads across the OS. Each entry in the file table is held in the
 * vector "table". This is a single level file system so the table that exists in the root directory is the only file
 * table in the system. Each thread wishing to open a file will have its own entry in the file table.The index of the
 * table will be used as the FD in methods that call for a FD, or a unique per-process ID number.
 *
 * Status: complete
 */
public class FileTable {
// File Structure Table

    private Vector<FileTableEntry> table;// the entity of File Structure Table
    private Directory dir;         // the root directory

    public FileTable(Directory directory) {// a default constructor
        table = new Vector<FileTableEntry>();// instantiate a file table
        dir = directory;                      // instantiate the root directory
    }

    //Zach implemented
    public synchronized FileTableEntry falloc(String fname, String mode) {
        // allocate/retrieve and register the corresponding inode using dir
        short iNodeNumber = dir.namei(fname);
        if (iNodeNumber == -1) { //If the file doesn't exist, create it
            iNodeNumber = dir.ialloc(fname);
        }

        Inode node = new Inode(iNodeNumber);

        // allocate a new file (structure) table entry for this file name
        FileTableEntry newEntry = new FileTableEntry(node, iNodeNumber, mode);
        table.add(newEntry);

        // increment this inode's count
        newEntry.count++;

        // immediately write back this inode to the disk
        newEntry.inode.toDisk(iNodeNumber);

        // return a reference to this file (structure) table entry
        return newEntry;
    }

    public synchronized boolean ffree(FileTableEntry e) {
        // receive a file table entry
        // free the file table entry corresponding to this index
        if (table.removeElement(e) == true) { // find this file table entry
            if (e == null) {
                notify();
                return true;
            }
            e.inode.count--;       // this entry no longer points to this inode
            switch (e.inode.flag) {
                case 1:
                    e.inode.flag = 0;
                    break;
                case 2:
                    e.inode.flag = 0;
                    break;
                case 4:
                    e.inode.flag = 3;
                    break;
                case 5:
                    e.inode.flag = 3;
                    break;
            }
            e.inode.toDisk(e.iNumber);     // reflect this inode to disk
            e = null;                        // this file table entry is erased.
            notify();
            return true;
        } else
            return false;
    }

    public synchronized boolean fempty() {
        return table.isEmpty();             // return if table is empty
    }// called before a format

    //Deletes all files
    public void freeAll() {
        int startingLength = table.size();
        while (!fempty()) {
            ffree(table.get(0));
        }
    }
}
