/**
 *
 * @author DLogic
 * @version 2.1
 */
package ufrsimplest;

import com.sun.jna.*;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class UfrCoder {

    public enum ERRORCODES {

        DL_OK(0x00),
        COMMUNICATION_ERROR(0x01),
        CHKSUM_ERROR(0x02),
        READING_ERROR(0x03),
        WRITING_ERROR(0x04),
        BUFFER_OVERFLOW(0x05),
        MAX_ADDRESS_EXCEEDED(0x06),
        MAX_KEY_INDEX_EXCEEDED(0x07),
        NO_CARD(0x08),
        COMMAND_NOT_SUPPORTED(0x09),
        FORBIDEN_DIRECT_WRITE_IN_SECTOR_TRAILER(0x0A),
        ADDRESSED_BLOCK_IS_NOT_SECTOR_TRAILER(0x0B),
        WRONG_ADDRESS_MODE(0x0C),
        WRONG_ACCESS_BITS_VALUES(0x0D),
        AUTH_ERROR(0x0E),
        PARAMETERS_ERROR(0x0F),
        MAX_SIZE_EXCEEDED(0x10),
        UNSUPPORTED_CARD_TYPE(0x11),
        COMMUNICATION_BREAK(0x50),
        NO_MEMORY_ERROR(0x51),
        CAN_NOT_OPEN_READER(0x52),
        READER_NOT_SUPPORTED(0x53),
        READER_OPENING_ERROR(0x54),
        READER_PORT_NOT_OPENED(0x55),
        CANT_CLOSE_READER_PORT(0x56),
        WRITE_VERIFICATION_ERROR(0x70),
        BUFFER_SIZE_EXCEEDED(0x71),
        VALUE_BLOCK_INVALID(0x72),
        VALUE_BLOCK_ADDR_INVALID(0x73),
        VALUE_BLOCK_MANIPULATION_ERROR(0x74),
        WRONG_UI_MODE(0x75),
        KEYS_LOCKED(0x76),
        KEYS_UNLOCKED(0x77),
        WRONG_PASSWORD(0x78),
        CAN_NOT_LOCK_DEVICE(0x79),
        CAN_NOT_UNLOCK_DEVICE(0x7A),
        DEVICE_EEPROM_BUSY(0x7B),
        RTC_SET_ERROR(0x7C),
        ANTICOLLISION_DISABLED(0x7D),
        NO_CARDS_ENUMERRATED(0x7E),
        CARD_ALREADY_SELECTED(0x7F),
        FT_STATUS_ERROR_1(0xA0),
        FT_STATUS_ERROR_2(0xA1),
        FT_STATUS_ERROR_3(0xA2),
        FT_STATUS_ERROR_4(0xA3),
        FT_STATUS_ERROR_5(0xA4),
        FT_STATUS_ERROR_6(0xA5),
        FT_STATUS_ERROR_7(0xA6),
        FT_STATUS_ERROR_8(0xA7),
        FT_STATUS_ERROR_9(0xA8);

        private int value;

        private ERRORCODES(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    protected static String sPlatformType = "";

    /**
     * specifies the type of platform isLinux() isWindows() isMac()
     *
     * @return .so , .dll or .dylib name based on the platform as String type
     */
    static public String GetPlatformType() {

        int os_type = Platform.getOSType();

        String lib_name = "uFCoder";
        String prefix = "";
        String postfix = "";
        String extension = "";

        switch (os_type) {
            case Platform.MAC:
                prefix = "lib";
                extension = "dylib";
                break;

            case Platform.LINUX:
                prefix = "lib";
                extension = "so";
                break;

            case Platform.WINDOWS:
                prefix = "";
                extension = "dll";
                break;
        }

        if (Platform.isARM()) {
            postfix = "armhf";
        } else if (Platform.isPPC()) {
            postfix = "ppc";
        } else if (Platform.isIntel()) {
            postfix = "x86";
        }

        if (Platform.is64Bit()) {
            postfix += "_64";
        }

        // patch for OSX
        if (os_type == Platform.MAC && Platform.isIntel()) {
            postfix = "";
        }

        sPlatformType = prefix + lib_name;

        if (postfix.length() > 0) {
            sPlatformType += "-" + postfix;
        }

        sPlatformType += "." + extension;

        System.out.println("lib: " + sPlatformType);

        return sPlatformType;
    }

    /**
     * Returns the full path to the required libraries
     *
     * @param isApplet
     * @return String
     */
    static public String GetLibFullPath(boolean isApplet) {

        String lib_path = System.getProperty("user.dir");

        switch(Platform.getOSType())
        {
            case Platform.UNSPECIFIED:
                // throw exception
                
                break;
                
            case Platform.WINDOWS:
                lib_path += "\\lib\\";
                
                break;

            case Platform.MAC:
            case Platform.LINUX:
            default:
                
                lib_path += "/lib/";
                
                break;
        }

        if (isApplet) {
            lib_path = ""; // only for applet
        }
        lib_path += GetPlatformType();

        System.out.println("GetLibFullPath(): " + lib_path);

        return lib_path;
    }

    public interface uFrFunctions extends Library {

        int GetReaderType(IntByReference reader_type);

        int GetReaderSerialNumber(IntByReference reader_serial);

        int LinearFormatCard(byte[] key_a, byte block_access_bits, byte sector_access_bits, byte sector_trailers_byte9,
                byte[] key_b, ShortByReference sector_formatted, byte auth_mode, byte key_index);

        int ReaderOpen();

        int ReaderClose();

        int GetReaderType(int[] iaReaderType);

        int GetCardId(ByteByReference bCardType, IntByReference iCardSerial);

        int GetCardIdEx(ByteByReference bCardType, byte[] baCardUID, ByteByReference bUidSize);

        int GetDlogicCardType(ByteByReference bDLCardType);

        int ReaderUISignal(int iLightMode, int iSoundMode);

        int LinearRead(byte[] baReadData, int iLinearAddress, int iDataLength, ShortByReference shBytesRet, byte bAuthMode, byte bKeyIndex);

        int LinearWrite(byte[] baWriteData, int iLinearAddress, int iDataLength, ShortByReference shBytesWritten, byte bAuthMode, byte bKeyIndex);

        int BlockWrite(byte[] saBlockWrite, int iBlockAddress, byte bAuthMode, byte bKeyIndex);

        int GetReaderSerialNumber(int[] reader_serial);

        int GetCardId(byte[] card_id, int[] card_serial);

        int ReaderSoftRestart();

        int ReaderReset();

        int ReadUserData(byte[] read_user_data);

        int WriteUserData(byte[] write_user_data);

        int ReaderKeyWrite(byte[] reader_key, byte key_index);

        int LinearRead(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_ret, byte auth_mode, int key_index);

        int LinearRead_AKM1(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_ret, byte auth_mode);

        int LinearRead_AKM2(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_ret, byte auth_mode);

        int LinearRead_PK(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_ret, byte auth_mode, byte[] pk_key);

        int LinearWrite(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_written, byte auth_mode, int key_index);

        int LinearWrite_AKM1(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_written, byte auth_mode);

        int LinearWrite_AKM2(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_written, byte auth_mode);

        int LinearWrite_PK(byte[] aucdata, int linear_address, int data_length, ShortByReference bytes_written, byte auth_mode, byte[] pk_key);

        int BlockRead(byte[] block_data, byte block_address, byte auth_mode, byte key_index);

        int BlockRead_AKM1(byte[] block_data, byte block_address, byte auth_mode);

        int BlockRead_AKM2(byte[] block_data, byte block_address, byte auth_mode);

        int BlockRead_PK(byte[] block_data, byte block_address, byte auth_mode, byte[] pk_key);

        int BlockWrite(byte[] block_data, byte block_address, byte auth_mode, byte key_index);

        int BlockWrite_AKM1(byte[] block_data, byte block_address, byte auth_mode);

        int BlockWrite_AKM2(byte[] block_data, byte block_address, byte auth_mode);

        int BlockWrite_PK(byte[] block_data, byte block_address, byte auth_mode, byte[] pk_key);

        int BlockInSectorRead(byte[] block_data, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int BlockInSectorRead_AKM1(byte[] block_data, byte sector_address, byte block_address, byte auth_mode);

        int BlockInSectorRead_AKM2(byte[] block_data, byte sector_address, byte block_address, byte auth_mode);

        int BlockInSectorRead_PK(byte[] block_data, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int BlockInSectorWrite(byte[] block_data, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int BlockInSectorWrite_AKM1(byte[] block_data, byte sector_address, byte block_address, byte auth_mode);

        int BlockInSectorWrite_AKM2(byte[] block_data, byte sector_address, byte block_address, byte auth_mode);

        int BlockInSectorWrite_PK(byte[] block_data, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockRead(IntByReference value_read, ShortByReference value_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockRead_AKM1(IntByReference value_read, ShortByReference value_address, byte block_address, byte auth_mode);

        int ValueBlockRead_AKM2(IntByReference value_read, ShortByReference value_address, byte block_address, byte auth_mode);

        int ValueBlockRead_PK(IntByReference value_read, ShortByReference value_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockWrite(int value_read, short value_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockWrite_AKM1(int value_read, short value_address, byte block_address, byte auth_mode);

        int ValueBlockWrite_AKM2(int value_read, short value_address, byte block_address, byte auth_mode);

        int ValueBlockWrite_PK(int value_read, short value_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockIncrement(int incr_value, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockIncrement_AKM1(int incr_value, byte block_address, byte auth_mode);

        int ValueBlockIncrement_AKM2(int incr_value, byte block_address, byte auth_mode);

        int ValueBlockIncrement_PK(int incr_value, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockDecrement(int incr_value, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockDecrement_AKM1(int incr_value, byte block_address, byte auth_mode);

        int ValueBlockDecrement_AKM2(int incr_value, byte block_address, byte auth_mode);

        int ValueBlockDecrement_PK(int incr_value, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockInSectorRead(IntByReference read_data, ShortByReference value_address, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockInSectorRead_AKM1(IntByReference read_data, ShortByReference value_address, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorRead_AKM2(IntByReference read_data, ShortByReference value_address, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorRead_PK(IntByReference read_data, ShortByReference value_address, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockInSectorWrite(int read_data, short value_address, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockInSectorWrite_AKM1(int read_data, short value_address, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorWrite_AKM2(int read_data, short value_address, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorWrite_PK(int read_data, short value_address, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockInSectorIncrement(int read_data, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockInSectorIncrement_AKM1(int read_data, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorIncrement_AKM2(int read_data, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorIncrement_PK(int read_data, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int ValueBlockInSectorDecrement(int read_data, byte sector_address, byte block_address, byte auth_mode, byte key_index);

        int ValueBlockInSectorDecrement_AKM1(int read_data, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorDecrement_AKM2(int read_data, byte sector_address, byte block_address, byte auth_mode);

        int ValueBlockInSectorDecrement_PK(int read_data, byte sector_address, byte block_address, byte auth_mode, byte[] pk_key);

        int LinearFormatCard(byte[] key_a, byte block_access_bits, byte sector_access_bits, byte sector_trailers_byte9,
                byte[] key_b, ByteByReference sector_formatted, byte auth_mode, byte key_index);

        int LinearFormatCard_AKM1(byte[] key_a, byte block_access_bits, byte sector_access_bits, byte sector_trailers_byte9,
                byte[] key_b, ByteByReference sector_formatted, byte auth_mode);

        int LinearFormatCard_AKM2(byte[] key_a, byte block_access_bits, byte sector_access_bits, byte sector_trailers_byte9,
                byte[] key_b, ByteByReference sector_formatted, byte auth_mode);

        int LinearFormatCard_PK(byte[] key_a, byte block_access_bits, byte sector_access_bits, byte sector_trailers_byte9,
                byte[] key_b, ByteByReference sector_formatted, byte auth_mode, byte[] pk_key);

        int SectorTrailerWrite(byte addressing_mode, byte address, byte[] key_a, byte block_access_bits0,
                byte block_access_bits1, byte block_access_bits2, byte sector_trailer_access_bits,
                byte sector_trailer_byte9, byte[] key_b, byte auth_mode, byte key_index);

        int SectorTrailerWrite_AKM1(byte addressing_mode, byte address, byte[] key_a, byte block_access_bits0,
                byte block_access_bits1, byte block_access_bits2, byte sector_trailer_access_bits,
                byte sector_trailer_byte9, byte[] key_b, byte auth_mode);

        int SectorTrailerWrite_AKM2(byte addressing_mode, byte address, byte[] key_a, byte block_access_bits0,
                byte block_access_bits1, byte block_access_bits2, byte sector_trailer_access_bits,
                byte sector_trailer_byte9, byte[] key_b, byte auth_mode);

        int SectorTrailerWrite_PK(byte addressing_mode, byte address, byte[] key_a, byte block_access_bits0,
                byte block_access_bits1, byte block_access_bits2, byte sector_trailer_access_bits,
                byte sector_trailer_byte9, byte[] key_b, byte auth_mode, byte[] pk_key);

    }
}
