package kr.co.elpusk.javapos.msr;

import java.lang.reflect.Constructor;

import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

public class Lpu237ServiceInstanceFactory implements JposServiceInstanceFactory
{
    public JposServiceInstance createInstance(String s, JposEntry jposentry)
            throws JposException {
        if (!jposentry.hasPropertyWithName("serviceClass")) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE,
                    "The JposEntry does not contain the 'serviceClass' property");
        }

        try {
            String s1 = (String) jposentry.getPropertyValue("serviceClass");
            if (s1 == null || s1.trim().isEmpty()) {
                throw new JposException(JposConst.JPOS_E_NOSERVICE,
                        "The 'serviceClass' property is empty");
            }
            Class<?> class1 = Class.forName(s1);
            Constructor<?> constructor = class1.getConstructor();
            Object instance = constructor.newInstance();
            if (!(instance instanceof JposServiceInstance)) {
                throw new JposException(JposConst.JPOS_E_NOSERVICE,
                        "The serviceClass does not implement JposServiceInstance: " + s1);
            }
            return (JposServiceInstance) instance;
        } catch (JposException jposException) {
            throw jposException;
        } catch (Exception exception) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE,
                    "Could not create the service instance!", exception);
        }
    }

}
