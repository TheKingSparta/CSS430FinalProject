import java.util.Vector;

public class FileTable {
// File Structure Table

    private Vector<FileTableEntry> table;// the entity of File Structure Table
    private Directory dir;         // the root directory

    public FileTable(Directory directory) {// a default constructor
        table = new Vector<FileTableEntry>();// instantiate a file table
        dir = directory;                      // instantiate the root directory
    }

    //TODO: you implement
    public synchronized FileTableEntry falloc(String fname, String mode) {
        // allocate a new file (structure) table entry for this file name
        FileTableEntry target = new FileTableEntry();
        table.add(target);
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry
        return target;
    }

    public synchronized boolean ffree(FileTableEntry e) {
        // receive a file table entry
        // free the file table entry corresponding to this index
        if (table.removeElement(e) == true) { // find this file table entry
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
    }                                        // called before a format
}
