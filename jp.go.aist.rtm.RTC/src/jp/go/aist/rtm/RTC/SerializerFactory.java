package jp.go.aist.rtm.RTC;

/**
 * {@.ja Serializer用ファクトリの実装}
 * {@.en Implement of factory for Serializer} 
 */
public class SerializerFactory<ABSTRACTCLASS,IDENTIFIER> extends FactoryGlobal<ABSTRACTCLASS,IDENTIFIER> {

    /**
     * {@.ja コンストラクタ。}
     * {@.en Constructor}
     */
    private SerializerFactory() {

    }
    /**
     * {@.ja インスタンス生成。}
     * {@.en Create instance}
     *
     * <p>
     * {@.ja インスタンスを生成する。}
     *
     * @return 
     *   {@.ja インスタンス}
     *   {@.en SerializerFactory object}
     *
     */
    synchronized public static SerializerFactory instance() {
        if (factory_global == null) {
            try {
                factory_global = new SerializerFactory();
            } catch (Exception e) {
                factory_global = null;
            }
        }

        return factory_global;
    }
    /**
     *  <p> object </p>
     */
    private static SerializerFactory factory_global;
}



