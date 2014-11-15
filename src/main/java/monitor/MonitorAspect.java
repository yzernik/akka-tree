package monitor;

import akka.actor.ActorRef;
import nworks.actortree.visualizer.Boot;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MonitorAspect {

//    private ActorSystemMessages asm = new ActorSystemMessages();

    @AfterReturning(pointcut = "execution (* akka.actor.ActorRefFactory.actorOf(..))",
            returning = "ref", argNames = "ref")
    public void actorCreationAdvice(ActorRef ref) {
        System.out.println("------------------ Actor created " + ref);
        if(!ref.path().toString().contains("actor-tree"))
           Boot.monitor().tell(new NewActorCreated(ref), ActorRef.noSender());
    }

//    @Pointcut(
//            value = "execution (* akka.actor.ActorCell.receiveMessage(..)) && args(msg)",
//            argNames = "msg")
//    public void receiveMessagePointcut(Object msg) {}
//
//
//    @Before(value = "receiveMessagePointcut(msg)",
//            argNames = "jp,msg")
//    public void message(JoinPoint jp, Object msg) {
//        // log the message
//        System.out.println("Message>>>>>>>>>>> " + msg);
//        asm.recordMessage();
//        System.out.println("Average throughput " + asm.average());
//    }
}