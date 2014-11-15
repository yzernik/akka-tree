package monitor;

import akka.actor.ActorRef;

public class NewActorCreated {

    public ActorRef ref;
    public NewActorCreated(ActorRef ref) {
      this.ref = ref;
    }
}
