package jp.go.aist.rtm.RTC;

/**
 * {@.ja CdrBuffer用ファクトリの実装。}
 * {@.en This class is a factory for CdrBuffer.}
 */
public class BufferFactory<ABSTRACTCLASS,IDENTIFIER> extends FactoryGlobal<ABSTRACTCLASS,IDENTIFIER> {

    /**
     * {@.ja コンストラクタ}
     * {@.en Constructor}
     */
    private BufferFactory() {

    }
    /**
     * {@.ja BufferFactoryのインスタンスを生成する。} 
     * {@.en Creates a instance of BufferFactory.}
     * @return
     *   {@.ja BufferFactoryオブジェクト}
     *   {@.en BufferFactory object}
     */
    synchronized public static BufferFactory instance() {
        if (factory_global == null) {
            try {
                factory_global = new BufferFactory();
            } catch (Exception e) {
                factory_global = null;
            }
        }

        return factory_global;
    }
    /**
     *  <p> object </p>
     */
    private static BufferFactory factory_global;
}

