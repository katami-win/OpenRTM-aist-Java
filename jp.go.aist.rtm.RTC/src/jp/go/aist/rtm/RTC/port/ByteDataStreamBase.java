package jp.go.aist.rtm.RTC.port;

import jp.go.aist.rtm.RTC.util.Properties;

/**
 * {@.ja シリアライザの基底クラス}
 * {@.en ByteDataStreamBase}
 *<p>
 */
public interface ByteDataStreamBase {

    /**
     * {@.ja 初期化関数(未使用)}
     * {@.en Initialization function (not used)}
     *
     * <p>
     * </p>
     * @param  prop
     *   {@.ja プロパティ(コネクタプロファイルから取得)}
     *   {@.en Properties (getted from connector profile)}
     *
     */
    void init(final Properties prop);
    /**
     * {@.ja 保持しているバッファにデータを書き込む}
     * {@.en writes data to a buffer that is held.}
     *
     * <p>
     * </p>
     * @param  buffer
     *   {@.ja 書き込み元のバッファ}
     *   {@.en Source buffer}
     * @param length データのサイズ
     *   {@.ja データのサイズ}
     *   {@.en Data size}
     *
     */
    void writeData(final byte[] buffer, long length);
    /**
     * {@.ja 引数のバッファにデータを書き込む}
     * {@.en  writes data to the argument buffer.}
     *
     * <p>
     * </p>
     * @param  buffer
     *   {@.ja 書き込み先のバッファ}
     *   {@.en Destination buffer}
     * @param length データのサイズ
     *   {@.ja データのサイズ}
     *   {@.en Data size}
     *
     */
    void readData(byte[] buffer, long length);
    /**
     * {@.ja データの長さを取得}
     * {@.en gets the length of the data.}
     *
     * @return 
     *   {@.ja データの長さ}
     *   {@.en data length}
     */
    long getDataLength();
    /**
     * {@.ja エンディアンを設定する}
     * {@.en Sets an endian.}
     *
     * @param little_endian
     *   {@.ja true: little, false: big}
     *   {@.en true: little, false: big}
     *
     */
    void isLittleEndian(boolean little_endian);

};

