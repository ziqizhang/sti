package uk.ac.shef.oak.any23.extension.extractor;

import org.apache.any23.configuration.DefaultConfiguration;
import org.apache.any23.extractor.ExtractorFactory;
import org.apache.any23.extractor.ExtractorGroup;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 30/10/12
 * Time: 10:56
 */
public class LExtractorRegistry {
    /**
         * The instance.
         */
        private static LExtractorRegistry instance = null;

        /**
         * maps containing the related {@link org.apache.any23.extractor.ExtractorFactory} for each
         * registered {@link org.apache.any23.extractor.Extractor}.
         */
        private Map<String, ExtractorFactory<?>> factories = new HashMap<String, ExtractorFactory<?>>();

        /**
         * @return returns the {@link LExtractorRegistry} instance.
         */
        public static LExtractorRegistry getInstance() {
            // Thread-safe
            synchronized (LExtractorRegistry.class) {
                final DefaultConfiguration conf = DefaultConfiguration.singleton();
                if (instance == null) {
                    instance = new LExtractorRegistry();
                    /*instance.register(RDFXMLExtractor.factory);
                    instance.register(TurtleExtractor.factory);
                    instance.register(NTriplesExtractor.factory);
                    instance.register(NQuadsExtractor.factory);
                    instance.register(TriXExtractor.factory);*/
                    if(conf.getFlagProperty("any23.extraction.rdfa.programmatic")) {
                        instance.register(LRDFa11Extractor.factory);
                    } else {
                        //instance.register(RDFaExtractor.factory);
                    }
                    /*instance.register(HeadLinkExtractor.factory);
                    instance.register(LicenseExtractor.factory);
                    instance.register(TitleExtractor.factory);
                    instance.register(XFNExtractor.factory);
                    instance.register(ICBMExtractor.factory);
                    instance.register(AdrExtractor.factory);
                    instance.register(GeoExtractor.factory);
                    instance.register(HCalendarExtractor.factory);
                    instance.register(HCardExtractor.factory);
                    instance.register(HListingExtractor.factory);
                    instance.register(HResumeExtractor.factory);
                    instance.register(HReviewExtractor.factory);
                    instance.register(HRecipeExtractor.factory);
                    instance.register(SpeciesExtractor.factory);
                    instance.register(TurtleHTMLExtractor.factory);*/
                    instance.register(LMicrodataExtractor.factory);
                    /*instance.register(CSVExtractor.factory);
                    if(conf.getFlagProperty("any23.extraction.head.meta")) {
                        instance.register(HTMLMetaExtractor.factory);
                    }*/
                }
            }
            return instance;
        }

        /**
         * Registers an {@link ExtractorFactory}.
         *
         * @param factory
         * @throws IllegalArgumentException if trying to register a {@link ExtractorFactory}
         *         with a that already exists in the registry.
         */
        public void register(ExtractorFactory<?> factory) {
            if (factories.containsKey(factory.getExtractorName())) {
                throw new IllegalArgumentException(String.format("Extractor name clash: %s",
                        factory.getExtractorName()));
            }
            factories.put(factory.getExtractorName(), factory);
        }

        /**
         *
         * Retrieves a {@link ExtractorFactory} given its name
         *
         * @param name of the desired factory
         * @return the {@link ExtractorFactory} associated to the provided name
         * @throws IllegalArgumentException if there is not a
         * {@link ExtractorFactory} associated to the provided name.
         */
        public ExtractorFactory<?> getFactory(String name) {
            if (!factories.containsKey(name)) {
                throw new IllegalArgumentException("Unregistered extractor name: " + name);
            }
            return factories.get(name);
        }

        /**
         * @return an {@link org.apache.any23.extractor.ExtractorGroup} with all the registered
         * {@link org.apache.any23.extractor.Extractor}.
         */
        public ExtractorGroup getExtractorGroup() {
            return getExtractorGroup(getAllNames());
        }

        /**
         * Returns an {@link ExtractorGroup} containing the
         * {@link ExtractorFactory} mathing the names provided as input.
         * @param names a {@link java.util.List} containing the names of the desired {@link ExtractorFactory}.
         * @return the extraction group.
         */
        public ExtractorGroup getExtractorGroup(List<String> names) {
            List<ExtractorFactory<?>> members = new ArrayList<ExtractorFactory<?>>(names.size());
            for (String name : names) {
                members.add(getFactory(name));
            }
            return new ExtractorGroup(members);
        }

        /**
         *
         * @param name of the {@link ExtractorFactory}
         * @return <code>true</code> if is there a {@link ExtractorFactory}
         * associated to the provided name.
         */
        public boolean isRegisteredName(String name) {
            return factories.containsKey(name);
        }

        /**
         * Returns the names of all registered extractors, sorted alphabetically.
         */
        public List<String> getAllNames() {
            List<String> result = new ArrayList<String>(factories.keySet());
            Collections.sort(result);
            return result;
        }
}
