/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.processor.attr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.NestableAttributeHolderNode;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.AbstractProcessor;
import org.thymeleaf.processor.AttributeNameProcessorMatcher;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.IProcessorMatcher;
import org.thymeleaf.processor.ProcessorMatchingContext;
import org.thymeleaf.processor.ProcessorResult;


/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 2.0.0
 *
 */
public abstract class AbstractAttrProcessor extends AbstractProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final IAttributeNameProcessorMatcher matcher; 
    
    
    protected AbstractAttrProcessor(final String attributeName) {
        this(new AttributeNameProcessorMatcher(attributeName));
    }

    protected AbstractAttrProcessor(final IAttributeNameProcessorMatcher matcher) {
        super();
        this.matcher = matcher;
    }

    
    public final IProcessorMatcher<? extends NestableAttributeHolderNode> getMatcher() {
        return this.matcher;
    }

    
    @Override
    protected final ProcessorResult doProcess(final Arguments arguments, final ProcessorMatchingContext processorMatchingContext, final Node node) {

        // Because of the type of applicability being used, casts to Element here will not fail
        final Element element = (Element) node;
        final String[] attributeNames = this.matcher.getAttributeNames(processorMatchingContext);

        String matchedAttributeName = null;
        for (final String attributeName : attributeNames) {
            if (element.hasNormalizedAttribute(attributeName)) {
                matchedAttributeName = attributeName;
                break;
            }
        }

        if (this.logger.isTraceEnabled()) {
            final String attributeValue = ((Element)node).getAttributeValueFromNormalizedName(matchedAttributeName);
            this.logger.trace("[THYMELEAF][{}][{}] Processing attribute \"{}\" with value \"{}\" in element \"{}\"",
                    new Object[] {TemplateEngine.threadIndex(), arguments.getTemplateName(), matchedAttributeName, (attributeValue == null? "" : attributeValue), ((Element)node).getNormalizedName()});
        }

        return processAttribute(arguments, element, matchedAttributeName);

    }
    
    protected abstract ProcessorResult processAttribute(final Arguments arguments, final Element element, final String attributeName);
    
    
}
