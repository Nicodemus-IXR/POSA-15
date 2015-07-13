package vandy.mooc.common;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * This Activity provides a framework for mediating access to the
 * Presenter layer in the Model-View-Presenter pattern.  It
 * automatically handles runtime configuration changes in conjunction
 * with an instance of OpsType, which must implement the PresenterOps
 * interface.  It extends LifecycleLoggingActivity so that all
 * lifecycle hook method calls are automatically logged.  It also
 * implements the ContextView interface that provides access to the
 * Activity and Application contexts.
 */
public abstract class GenericActivity<RequiredViewOps,
                                      ProvidedViewOps,
                                      OpsType extends PresenterOps<RequiredViewOps>>
       extends LifecycleLoggingActivity
       implements ContextView {
    /**
     * Used to retain the ProvidedViewOps state between runtime
     * configuration changes.
     */
    private final RetainedFragmentManager mRetainedFragmentManager 
        = new RetainedFragmentManager(this.getFragmentManager(),
                                      TAG);
 
    /**
     * Instance of the operations ("Ops") type.
     */
    private OpsType mOpsInstance;

    /**
     * Lifecycle hook method that's called when the GenericActivity is
     * created.
     *
     * @param savedInstanceState
     *            Object that contains saved state information.
     * @param opsType 
     *            Class object that's used to create an operations
     *            ("Ops") object.  
     * @param view
     *            Reference to the RequiredViewOps in the View layer.
     */
    public void onCreate(Bundle savedInstanceState,
                         Class<OpsType> opsType,
                         RequiredViewOps view) {
        // Call up to the super class.
        super.onCreate(savedInstanceState);

        // Handle configuration-related events, including the
        // initial creation of an Activity and any subsequent
        // runtime configuration changes.
        try {
            // If this method returns true it's the first time the
            // Activity has been created.
            if (mRetainedFragmentManager.firstTimeIn()) {
                Log.d(TAG,
                      "First time onCreate() call");

                // Initialize the GenericActivity fields.
                initialize(opsType,
                           view);
            } else {
                // The RetainedFragmentManager was previously
                // initialized, which means that a runtime
                // configuration change occured.
                Log.d(TAG,
                      "Second or subsequent onCreate() call");
                reinitialize(opsType,
                             view);
            }
        } catch (InstantiationException
                 | IllegalAccessException e) {
            Log.d(TAG, 
                  "handleConfiguration " 
                  + e);
            // Propagate this as a runtime exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the GenericActivity fields.
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void initialize(Class<OpsType> opsType,
                            RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        // Create the OpsType object.
        mOpsInstance = opsType.newInstance();

        // Put the OpsInstance into the RetainedFragmentManager under
        // the simple name.
        mRetainedFragmentManager.put(opsType.getSimpleName(),
                                     mOpsInstance);

        // Perform the first initialization.
        mOpsInstance.onCreate(view);
    }

    /**
     * Reinitialize the GenericActivity fields after a runtime
     * configuration change.
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void reinitialize(Class<OpsType> opsType,
                              RequiredViewOps view)
            throws InstantiationException, IllegalAccessException {
        // Try to obtain the OpsType instance from the
        // RetainedFragmentManager.
        mOpsInstance =
            mRetainedFragmentManager.get(opsType.getSimpleName());

        // This check shouldn't be necessary under normal
        // circumstances, but it's better to lose state than to
        // crash!
        if (mOpsInstance == null) 
            // Initialize the GenericActivity fields.
            initialize(opsType,
                       view);
        else
            // Inform it that the runtime configuration change has
            // completed.
            mOpsInstance.onConfigurationChange(view);
    }

    /**
     * Return the initialized ProvidedOps instance for use by the
     * application.
     */
    @SuppressWarnings("unchecked")
    public ProvidedViewOps getOps() {
        return (ProvidedViewOps) mOpsInstance;
    }

    /**
     * Return the RetainedFragmentManager.
     */
    public RetainedFragmentManager getRetainedFragmentManager() {
        return mRetainedFragmentManager;
    }

    /**
     * Return the Activity context.
     */
    @Override
    public Context getActivityContext() {
        return this;
    }
    
    /**
     * Return the Application context.
     */
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
}
