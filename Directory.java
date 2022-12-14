/**
 * Worked on by: Renee
 * Purpose: Each inode represents one file, creates each inode and reads/writes it to the disk. The inodes are kept as
 * references in the FileTableEntry.
 *
 * Status: complete but not tested
 */

public class Directory {
    private static int maxChars = 30; // the max characters of each file name

    private int fsizes[];             // the actual size of each file name
    private char fnames[][];          // file names in characters

    public Directory(int maxInumber) {       // a default constructor
        fsizes = new int[maxInumber];           // maxInumber = max files
        for (int i = 0; i < maxInumber; i++)  // all file sizes set to 0
            fsizes[i] = 0;
        fnames = new char[maxInumber][maxChars];

        String root = "/";                      // entry(inode) 0 is "/"
        fsizes[0] = root.length();
        root.getChars(0, fsizes[0], fnames[0], 0);
    }

    public void bytes2directory(byte data[]) {
        // assumes data[] contains directory information retrieved from disk
        // initialize the directory fsizes[] and fnames[] with this data[]
        // fsizes and fnames should already have the correct lengths
        int currDataIndex = 0;
        //First fsizes.length bytes: fsizes
        for (; currDataIndex < fsizes.length; currDataIndex++) {
            //There's not any documentation about the "offset" (second) parameter of bytes2int, so this may be wrong
            fsizes[currDataIndex] = SysLib.bytes2int(data, currDataIndex * 4);
        }
        //Next fnames.length bytes: fnames
        for (; currDataIndex < fnames.length; currDataIndex++) {

        }
    }

    public byte[] directory2bytes() {
        // converts and return directory information into a plain byte array
        // this byte array will be written back to disk
        byte[] data = new byte[fsizes.length * 4 + fnames.length * maxChars * 2];
        int offset = 0;
        for (int i = 0; i < fsizes.length; i++, offset += 4)
            SysLib.int2bytes(fsizes[i], data, offset);

        for (int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
            String tableEntry = new String(fnames[i], 0, fsizes[i]);
            byte[] bytes = tableEntry.getBytes();
            System.arraycopy(bytes, 0, data, offset, bytes.length);
        }
        return data;
    }

    public short ialloc(String filename) {
        // filename is the name of a file to be created.
        // allocates a new inode number for this filename.
        short i;
        // i = 0 is reserved
        for (i = 1; i < fsizes.length; i++) {
            if (fsizes[i] == 0) {
                fsizes[i] = Math.min(filename.length(), maxChars);
                filename.getChars(0, fsizes[i], fnames[i], 0);
                return i;
            }
        }
        return -1;
    }

    public boolean ifree(short iNumber) {
        // deallocates this inumber (inode number).
        // the corresponding file will be deleted.
        //reset fsizes
        fsizes[iNumber] = 0;
        //Reset fnames
        for (int i = 0; i < fnames[iNumber].length; i++) {
            //fnames should probably bet set to something else, but I'm not sure what would be better. ' '?
            fnames[iNumber][i] = 0;
        }
        return true;
    }

    public short namei(String filename) {
        // returns the inumber corresponding to this filename
        short i;
        for (i = 0; i < fsizes.length; i++) {
            if (fsizes[i] == filename.length()) {
                String tableEntry = new String(fnames[i], 0, fsizes[i]);
                if (filename.compareTo(tableEntry) == 0)
                    return i;
            }
        }
        return -1;
    }


}
