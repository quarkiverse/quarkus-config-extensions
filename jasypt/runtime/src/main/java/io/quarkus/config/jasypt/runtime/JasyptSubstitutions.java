package io.quarkus.config.jasypt.runtime;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.normalization.Normalizer;
import org.jasypt.salt.RandomSaltGenerator;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Inject;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.TargetElement;

public class JasyptSubstitutions {
    @TargetClass(Normalizer.class)
    static final class Target_Normalizer {
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private static Boolean useIcuNormalizer;

        @Substitute
        static void initializeIcu4j() throws ClassNotFoundException {
            useIcuNormalizer = Boolean.FALSE;
        }

        @Substitute
        static char[] normalizeWithIcu4j(final char[] message) {
            throw new UnsupportedOperationException();
        }
    }

    @TargetClass(RandomSaltGenerator.class)
    static final class Target_RandomSaltGenerator {
        @Alias
        @InjectAccessors(SecureRandomAccessor.class)
        private SecureRandom random;
        @Inject
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
        String secureRandomAlgorithm;

        @Substitute
        @TargetElement(name = TargetElement.CONSTRUCTOR_NAME)
        public Target_RandomSaltGenerator(final String secureRandomAlgorithm) {
            this.secureRandomAlgorithm = secureRandomAlgorithm;
        }
    }

    @TargetClass(RandomIvGenerator.class)
    static final class Target_RandomIvGenerator {
        @Alias
        @InjectAccessors(SecureRandomAccessor.class)
        private SecureRandom random;
        @Inject
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
        String secureRandomAlgorithm;

        @Substitute
        @TargetElement(name = TargetElement.CONSTRUCTOR_NAME)
        public Target_RandomIvGenerator(final String secureRandomAlgorithm) {
            this.secureRandomAlgorithm = secureRandomAlgorithm;
        }
    }
}

class SecureRandomAccessor {
    private static volatile SecureRandom RANDOM;

    static SecureRandom get(Object instance) {
        SecureRandom result = RANDOM;
        if (result == null) {
            /* Lazy initialization on first access. */
            result = initializeOnce(getAlgorithm(instance));
        }
        return result;
    }

    static void set(Object instance, SecureRandom secureRandom) {
        throw new UnsupportedOperationException();
    }

    private static synchronized SecureRandom initializeOnce(String algorithm) {
        SecureRandom result = RANDOM;
        if (result != null) {
            /* Double-checked locking is OK because INSTANCE is volatile. */
            return result;
        }

        try {
            result = SecureRandom.getInstance(algorithm);
            RANDOM = result;
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionInitializationException(e);
        }
    }

    private static String getAlgorithm(Object instance) {
        if (instance instanceof JasyptSubstitutions.Target_RandomIvGenerator) {
            return ((JasyptSubstitutions.Target_RandomIvGenerator) (instance)).secureRandomAlgorithm;
        } else if (instance instanceof JasyptSubstitutions.Target_RandomSaltGenerator) {
            return ((JasyptSubstitutions.Target_RandomSaltGenerator) (instance)).secureRandomAlgorithm;
        } else {
            return "SHA1PRNG";
        }
    }
}
