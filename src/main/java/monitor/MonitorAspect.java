package monitor;

import akka.actor.ActorRef;
import nworks.actortree.visualizer.Boot;
import nworks.actortree.visualizer.actors.ActorMailboxSizeChanged;
import nworks.actortree.visualizer.actors.NewActorCreated;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;


import akka.actor.ActorCell;

@Aspect
public class MonitorAspect {

    @AfterReturning(pointcut = "execution (* akka.actor.ActorRefFactory.actorOf(..))",
            returning = "ref", argNames = "ref")
    public void actorCreationAdvice(ActorRef ref) {
        System.out.println("------------------ Actor created " + ref);
        if(!ref.path().toString().contains("actor-tree"))
           Boot.monitor().tell(new NewActorCreated(ref), ActorRef.noSender());
    }

    @Pointcut(value = "execution(* akka.actor.ActorCell.sendMessage(..)) && this(cell)")
    public void sendingMessageToActorCell(ActorCell cell) {}

    @After("sendingMessageToActorCell(cell)")
    public void afterSendMessageToActorCell(ActorCell cell) {
     if(!cell.self().path().toString().contains("actor-tree")) {
         Integer messageCount = cell.mailbox().numberOfMessages();
         System.out.println("------------------ Actor " + cell.self().path().name() + " " + messageCount);
         Boot.monitor().tell(new ActorMailboxSizeChanged(cell.self(), messageCount), ActorRef.noSender());
     }
    }

}