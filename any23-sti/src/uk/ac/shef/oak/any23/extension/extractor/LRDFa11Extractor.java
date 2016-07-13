package uk.ac.shef.oak.any23.extension.extractor;

import org.apache.any23.extractor.*;
import org.apache.any23.extractor.rdfa.RDFa11Extractor;
import org.apache.any23.extractor.rdfa.RDFa11ParserException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 30/10/12
 * Time: 09:19
 */
public class LRDFa11Extractor extends RDFa11Extractor {
    public final static String NAME = "lodie-html-rdfa11";

        public final static ExtractorFactory<LRDFa11Extractor> factory = new LRDFa11ExtractorFactory();

        private final LRDFa11Parser parser;

         private boolean verifyDataType;

         private boolean stopAtFirstError;

         /**
          * Constructor, allows to specify the validation and error handling policies.
          *
          * @param verifyDataType if <code>true</code> the data types will be verified,
          *         if <code>false</code> will be ignored.
          * @param stopAtFirstError if <code>true</code> the parser will stop at first parsing error,
          *        if <code>false</code> will ignore non blocking errors.
          */
         public LRDFa11Extractor(boolean verifyDataType, boolean stopAtFirstError) {
             this.parser = new LRDFa11Parser();
             this.verifyDataType   = verifyDataType;
             this.stopAtFirstError = stopAtFirstError;
         }

         /**
          * Default constructor, with no verification of data types and not stop at first error.
          */
         public LRDFa11Extractor() {
             this(false, false);
         }

         public boolean isVerifyDataType() {
             return verifyDataType;
         }

         public void setVerifyDataType(boolean verifyDataType) {
             this.verifyDataType = verifyDataType;
         }

         public boolean isStopAtFirstError() {
             return stopAtFirstError;
         }

         public void setStopAtFirstError(boolean stopAtFirstError) {
             this.stopAtFirstError = stopAtFirstError;
         }

         public void run(
                 ExtractionParameters extractionParameters,
                 ExtractionContext extractionContext,
                 Document in,
                 ExtractionResult out
         ) throws IOException, ExtractionException {
             try {
                 LExtractionResultImpl outWriter = (LExtractionResultImpl)out;
                 parser.processDocument( new URL(extractionContext.getDocumentURI().toString() ), in, outWriter );
             } catch (RDFa11ParserException rpe) {
                 throw new ExtractionException("Error while performing extraction.", rpe);
             } catch (ClassCastException cce){
                 throw new ExtractionException("Required: "+LExtractionResultImpl.class.getName(), cce);
             }
         }

         /**
          * @return the {@link org.apache.any23.extractor.ExtractorDescription} of this extractor
          */
         public ExtractorDescription getDescription() {
             return factory;
         }
}
