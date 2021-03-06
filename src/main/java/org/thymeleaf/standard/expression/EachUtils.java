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
package org.thymeleaf.standard.expression;

import org.thymeleaf.Configuration;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.util.DOMUtils;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;


/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 2.1.0
 *
 */
public final class EachUtils {


    private static final String OPERATOR = ":";
    private static final String STAT_SEPARATOR = ",";





    public static Each parseEach(
            final Configuration configuration, final IProcessingContext processingContext, final String input) {

        Validate.notNull(configuration, "Configuration cannot be null");
        Validate.notNull(processingContext, "Processing Context cannot be null");
        Validate.notNull(input, "Input cannot be null");

        final String preprocessedInput =
                    StandardExpressionPreprocessor.preprocess(configuration, processingContext, input);

        if (configuration != null) {
            final Each cachedEach = ExpressionCache.getEachFromCache(configuration, preprocessedInput);
            if (cachedEach != null) {
                return cachedEach;
            }
        }

        final Each each = internalParseEach(DOMUtils.unescapeXml(preprocessedInput.trim(), true));

        if (each == null) {
            throw new TemplateProcessingException("Could not parse as each: \"" + input + "\"");
        }

        if (configuration != null) {
            ExpressionCache.putEachIntoCache(configuration, preprocessedInput, each);
        }

        return each;

    }




    static Each internalParseEach(final String input) {

        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        final ExpressionParsingState decomposition =
                ExpressionParsingUtil.decompose(input,ExpressionParsingDecompositionConfig.DECOMPOSE_ALL_AND_UNNEST);

        if (decomposition == null) {
            return null;
        }

        return composeEach(decomposition, 0);

    }




    private static Each composeEach(final ExpressionParsingState state, final int nodeIndex) {

        if (state == null || nodeIndex >= state.size()) {
            return null;
        }

        if (state.hasExpressionAt(nodeIndex)) {
            // shouldn't happen in this case (ExpressionSequences are not Expressions). We need a string to parse!
            return null;
        }

        final String input = state.get(nodeIndex).getInput();

        if (StringUtils.isEmptyOrWhitespace(input)) {
            return null;
        }

        // First, check whether we are just dealing with a pointer input
        final int pointer = ExpressionParsingUtil.parseAsSimpleIndexPlaceholder(input);
        if (pointer != -1) {
            return composeEach(state, pointer);
        }

        final int inputLen = input.length();

        final int operatorLen = OPERATOR.length();
        final int operatorPos = input.indexOf(OPERATOR);
        if (operatorPos == -1 || operatorPos == 0 || operatorPos >= (inputLen - operatorLen)) {
            return null;
        }

        final String left = input.substring(0,operatorPos).trim();
        final String iterableStr = input.substring(operatorPos + operatorLen).trim();

        final int statPos = left.indexOf(STAT_SEPARATOR);
        final String iterVarStr;
        final String statusVarStr;
        if (statPos == -1) {
            iterVarStr = left;
            statusVarStr = null;
        } else {
            if (statPos == 0 || statPos >= (left.length() - operatorLen)) {
                return null;
            }
            iterVarStr = left.substring(0, statPos);
            statusVarStr = left.substring(statPos + operatorLen);
        }

        final Expression iterVarExpr = ExpressionParsingUtil.parseAndCompose(state, iterVarStr);
        if (iterVarStr == null) {
            return null;
        }

        final Expression statusVarExpr;
        if (statusVarStr != null) {
            statusVarExpr = ExpressionParsingUtil.parseAndCompose(state, statusVarStr);
            if (statusVarExpr == null) {
                return null;
            }
        } else {
            statusVarExpr = null;
        }

        final Expression iterableExpr = ExpressionParsingUtil.parseAndCompose(state, iterableStr);
        if (iterableExpr == null) {
            return null;
        }

        return new Each(iterVarExpr,statusVarExpr,iterableExpr);

    }




    private EachUtils() {
        super();
    }

}
