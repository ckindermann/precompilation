package uk.ac.man.cs.ont;

import java.util.TimerTask;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.*;


public class InterruptReasonerTask extends TimerTask {

    private OWLReasoner reasoner;
    private boolean interrupted;

    public InterruptReasonerTask(OWLReasoner reasoner) {
        super();
        this.reasoner = reasoner;
    }

    @Override
    public void run() {
        if (reasoner != null) {
            try {
                System.out.println("Timeout is exceeded");
                reasoner.interrupt();//this should throw a ReasonerInterruptedException
                //throw new ReasonerInterruptedException("Reasoner got interrupted due to timeout");
            } catch (Exception e) {
                System.out.println("Exception while reasoner interruption");
            } finally {
                reasoner.dispose();
            }
        }
    } 

    public boolean wasInterrupted(){
        return this.interrupted; 
    }
}
