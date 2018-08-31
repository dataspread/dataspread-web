package api;

import java.util.logging.Logger;

public class InteractionManager {

    private static final Logger logger = Logger.getLogger(InteractionManager.class.getName());
    private static InteractionManager instance;



    private InteractionManager()
    {
        logger.info("InteractionManager Created");
    }

    public static InteractionManager getInstance()
    {
        if (instance==null)
            instance=new InteractionManager();
        return instance;
    }




}
