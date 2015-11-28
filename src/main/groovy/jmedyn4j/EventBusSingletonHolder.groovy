package jmedyn4j

import groovy.util.logging.Slf4j;
import net.engio.mbassy.bus.MBassador
import net.engio.mbassy.bus.config.BusConfiguration
import net.engio.mbassy.bus.config.Feature


class EventBusSingletonHolder {
	public static final MBassador EventBus = new MBassador(new BusConfiguration()
		.addFeature(Feature.SyncPubSub.Default())
		.addFeature(Feature.AsynchronousHandlerInvocation.Default())
		.addFeature(Feature.AsynchronousMessageDispatch.Default())
		 
	);
}
