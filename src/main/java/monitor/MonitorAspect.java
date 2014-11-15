package monitor;

import akka.actor.ActorRef;
import nworks.actortree.visualizer.Boot;
import nworks.actortree.visualizer.actors.NewActorCreated;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MonitorAspect {

    @AfterReturning(pointcut = "execution (* akka.actor.ActorRefFactory.actorOf(..))",
            returning = "ref", argNames = "ref")
    public void actorCreationAdvice(ActorRef ref) {
        System.out.println("------------------ Actor created " + ref);
        if(!ref.path().toString().contains("actor-tree"))
           Boot.monitor().tell(new NewActorCreated(ref), ActorRef.noSender());
    }

}