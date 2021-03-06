package jp.go.aist.rtm.RTC;

import jp.go.aist.rtm.RTC.buffer.CdrRingBuffer;
import jp.go.aist.rtm.RTC.PeriodicTask;
import jp.go.aist.rtm.RTC.port.publisher.PublisherFlush;
import jp.go.aist.rtm.RTC.port.publisher.PublisherNew;
import jp.go.aist.rtm.RTC.port.publisher.PublisherPeriodic;
import jp.go.aist.rtm.RTC.port.InPortCorbaCdrProvider;
import jp.go.aist.rtm.RTC.port.InPortCorbaCdrConsumer;
import jp.go.aist.rtm.RTC.port.OutPortCorbaCdrProvider;
import jp.go.aist.rtm.RTC.port.OutPortCorbaCdrConsumer;
import jp.go.aist.rtm.RTC.port.InPortDirectProvider;
import jp.go.aist.rtm.RTC.port.InPortDirectConsumer;
import jp.go.aist.rtm.RTC.port.OutPortDirectProvider;
import jp.go.aist.rtm.RTC.port.OutPortDirectConsumer;
import jp.go.aist.rtm.RTC.port.InPortSHMProvider;
import jp.go.aist.rtm.RTC.port.InPortSHMConsumer;
import jp.go.aist.rtm.RTC.port.OutPortSHMProvider;
import jp.go.aist.rtm.RTC.port.OutPortSHMConsumer;
import jp.go.aist.rtm.RTC.port.InPortDSProvider;
import jp.go.aist.rtm.RTC.port.InPortDSConsumer;
import jp.go.aist.rtm.RTC.port.OutPortDSProvider;
import jp.go.aist.rtm.RTC.port.OutPortDSConsumer;
import jp.go.aist.rtm.RTC.port.CORBA_CdrSerializer;

import jp.go.aist.rtm.RTC.ComponentObserverConsumer;

/**
 * {@.ja Factory初期処理用クラス}
 * {@.en Class for Factory initial processing}
 */
public class FactoryInit {
    /**
     * {@.ja コンストラクタ}
     * {@.en Constructor}
     */
    public FactoryInit() {
    }

    /**
     * {@.ja Factory初期化処理}
     * {@.en Factory initialization}
     */
    public static void init() {
	// Buffers
	CdrRingBuffer.CdrRingBufferInit();
	
	// Threads
	PeriodicTask.PeriodicTaskInit();
	
	// Publishers
	PublisherFlush.PublisherFlushInit();
	PublisherNew.PublisherNewInit();
	PublisherPeriodic.PublisherPeriodicInit();
	
	// Providers/Consumer
	InPortCorbaCdrProvider.InPortCorbaCdrProviderInit();
	InPortCorbaCdrConsumer.InPortCorbaCdrConsumerInit();
	OutPortCorbaCdrConsumer.OutPortCorbaCdrConsumerInit();
	OutPortCorbaCdrProvider.OutPortCorbaCdrProviderInit();

	InPortDirectProvider.InPortDirectProviderInit();
	InPortDirectConsumer.InPortDirectConsumerInit();
	OutPortDirectProvider.OutPortDirectProviderInit();
	OutPortDirectConsumer.OutPortDirectConsumerInit();

	InPortSHMProvider.InPortSHMProviderInit();
	InPortSHMConsumer.InPortSHMConsumerInit();
	OutPortSHMProvider.OutPortSHMProviderInit();
	OutPortSHMConsumer.OutPortSHMConsumerInit();

	InPortDSProvider.InPortDSProviderInit();
	InPortDSConsumer.InPortDSConsumerInit();
	OutPortDSProvider.OutPortDSProviderInit();
	OutPortDSConsumer.OutPortDSConsumerInit();

        DefaultNumberingPolicy.DefaultNumberingPolicyInit();
        NodeNumberingPolicy.NodeNumberingPolicyInit();
        NamingServiceNumberingPolicy.NamingServiceNumberingPolicyInit();

        ComponentObserverConsumer.ComponentObserverConsumerInit();
        //ExtendedFsmServiceProvider.ExtendedFsmServiceProviderInit();

        CORBA_CdrSerializer.CORBA_CdrSerializerInit();

    }
}
