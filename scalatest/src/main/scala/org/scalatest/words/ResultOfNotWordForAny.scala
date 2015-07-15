/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.words

import org.scalatest.matchers._
import org.scalatest.enablers._
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import scala.util.matching.Regex
import java.lang.reflect.Field
import scala.reflect.Manifest
import org.scalatest.MatchersHelper.transformOperatorChars
import scala.collection.Traversable
import org.scalatest.Assertions.areEqualComparingArraysStructurally
import scala.collection.GenTraversable
import scala.collection.GenSeq
import scala.collection.GenMap
import org.scalactic.Tolerance
import org.scalactic.Explicitly
import org.scalactic.TripleEqualsSupport.Spread
import org.scalactic.TripleEqualsSupport.TripleEqualsInvocation
import scala.annotation.tailrec
import org.scalactic.Equality
import org.scalatest.MatchersHelper.andMatchersAndApply
import org.scalatest.MatchersHelper.orMatchersAndApply
// SKIP-SCALATESTJS-START
import org.scalatest.MatchersHelper.matchSymbolToPredicateMethod
// SKIP-SCALATESTJS-END
import org.scalatest.{FailureMessages, UnquotedString}
import org.scalatest.MatchersHelper.newTestFailedException
import org.scalatest.MatchersHelper.fullyMatchRegexWithGroups
import org.scalatest.MatchersHelper.startWithRegexWithGroups
import org.scalatest.MatchersHelper.endWithRegexWithGroups
import org.scalatest.MatchersHelper.includeRegexWithGroups
import org.scalatest.exceptions.NotAllowedException
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepthFun
import org.scalactic.Prettifier
import org.scalactic.Every
import org.scalatest.Assertion
import org.scalatest.Succeeded

/**
 * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="Matchers.html"><code>Matchers</code></a> for an overview of
 * the matchers DSL.
 *
 * @author Bill Venners
 */
sealed class ResultOfNotWordForAny[T](val left: T, val shouldBeTrue: Boolean) {

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not equal (7)
   *                   ^
   * </pre>
   */
  def equal(right: Any)(implicit equality: Equality[T]): Assertion = {
    if (equality.areEqual(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotEqual(left, right)
        else
          FailureMessages.equaled(left, right)
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be (7)
   *                   ^
   * </pre>
   */
  def be(right: Any) {
    if ((left == right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotEqualTo(left, right)
        else
          FailureMessages.wasEqualTo(left, right)
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be &lt;= (7)
   *                   ^
   * </pre>
   */
  def be(comparison: ResultOfLessThanOrEqualToComparison[T]): Assertion = {
    if (comparison(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotLessThanOrEqualTo(left, comparison.right)
        else
          FailureMessages.wasLessThanOrEqualTo(left, comparison.right)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be &gt;= (7)
   *                   ^
   * </pre>
   */
  def be(comparison: ResultOfGreaterThanOrEqualToComparison[T]): Assertion = {
    if (comparison(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotGreaterThanOrEqualTo(left, comparison.right)
        else
          FailureMessages.wasGreaterThanOrEqualTo(left, comparison.right)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be &lt; (7)
   *                   ^
   * </pre>
   */
  def be(comparison: ResultOfLessThanComparison[T]): Assertion = {
    if (comparison(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotLessThan(left, comparison.right)
        else
          FailureMessages.wasLessThan(left, comparison.right)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be &gt; (7)
   *                   ^
   * </pre>
   */
  def be(comparison: ResultOfGreaterThanComparison[T]): Assertion = {
    if (comparison(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotGreaterThan(left, comparison.right)
        else
          FailureMessages.wasGreaterThan(left, comparison.right)
      )
    }
    else Succeeded
  }

  /**
   * <strong>
   * The deprecation period for the "be ===" syntax has expired, and the syntax 
   * will now throw <code>NotAllowedException</code>.  Please use should equal, should ===, shouldEqual,
   * should be, or shouldBe instead.
   * </strong>
   *
   * <p>
   * Note: usually syntax will be removed after its deprecation period. This was left in because otherwise the syntax could in some
   * cases still compile, but silently wouldn't work.
   * </p>
   */
  @deprecated("The deprecation period for the be === syntax has expired. Please use should equal, should ===, shouldEqual, should be, or shouldBe instead.")
  def be(comparison: TripleEqualsInvocation[_]): Matcher[Any] = {
    throw new NotAllowedException(FailureMessages.beTripleEqualsNotAllowed,
                                  getStackDepthFun("ResultOfNotWordForAny.scala", "be")) 
  }

  /**
   * This method enables the following syntax, where <code>odd</code> refers to
   * a <code>BeMatcher[Int]</code>:
   *
   * <pre class="stHighlight">
   * 2 should not be (odd)
   *              ^
   * </pre>
   */
  def be(beMatcher: BeMatcher[T]): Assertion = {
    val result = beMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          result.failureMessage
        else
          result.negatedFailureMessage
      )
    }
    else Succeeded
  }
  
  /**
   * This method enables the following syntax, where <code>positiveNumber</code> refers to
   * an <code>AMatcher[Int]</code>:
   *
   * <pre class="stHighlight">
   * 2 should not be a (positiveNumber)
   *              ^
   * </pre>
   */
  def be(resultOfAWordToAMatcherApplication: ResultOfAWordToAMatcherApplication[T]): Assertion = {
    val aMatcher = resultOfAWordToAMatcherApplication.aMatcher
    val result = aMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          result.failureMessage
        else
          result.negatedFailureMessage
      )
    }
    else Succeeded
  }
  
  /**
   * This method enables the following syntax, where <code>oddNumber</code> refers to
   * an <code>AnMatcher[Int]</code>:
   *
   * <pre class="stHighlight">
   * 2 should not be an (oddNumber)
   *              ^
   * </pre>
   */
  def be(resultOfAnWordToAnMatcherApplication: ResultOfAnWordToAnMatcherApplication[T]): Assertion = {
    val anMatcher = resultOfAnWordToAnMatcherApplication.anMatcher
    val result = anMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          result.failureMessage
        else
          result.negatedFailureMessage
      )
    }
    else Succeeded
  }

  import scala.language.experimental.macros
  
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be a [Book]
   *                   ^
   * </pre>
   */
  def be(aType: ResultOfATypeInvocation[_]) = macro TypeMatcherMacro.checkATypeShouldBeTrueImpl
  
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not be a [Book]
   *                   ^
   * </pre>
   */
  def be(anType: ResultOfAnTypeInvocation[_]) = macro TypeMatcherMacro.checkAnTypeShouldBeTrueImpl

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * sevenDotOh should not be (6.5 +- 0.2)
   *                       ^
   * </pre>
   */
  def be(spread: Spread[T]): Assertion = {
    if (spread.isWithin(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotPlusOrMinus(left, spread.pivot, spread.tolerance)
        else
          FailureMessages.wasPlusOrMinus(left, spread.pivot, spread.tolerance)
      )
    }
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * partialFun should not be definedAt ("apple")
   *                       ^
   * </pre>
   */
  def be[U](resultOfDefinedAt: ResultOfDefinedAt[U])(implicit ev: T <:< PartialFunction[U, _]): Assertion = {
    if (left.isDefinedAt(resultOfDefinedAt.right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotDefinedAt(left, resultOfDefinedAt.right)
        else
          FailureMessages.wasDefinedAt(left, resultOfDefinedAt.right)
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * sevenDotOh should not equal (6.5 +- 0.2)
   *                       ^
   * </pre>
   */
  def equal(spread: Spread[T]): Assertion = {
    if (spread.isWithin(left) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotEqualPlusOrMinus(left, spread.pivot, spread.tolerance)
        else
          FailureMessages.equaledPlusOrMinus(left, spread.pivot, spread.tolerance)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * result should not equal null
   *                   ^
   * </pre>
   */
  def equal(right: Null): Assertion = {
    if ((left == null) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotEqualNull(left)
        else
          FailureMessages.equaledNull
      )
    }
    else Succeeded
  }

  /**
   * Enables parentheses to be placed around <code>length (N)</code> in expressions of the
   * form: <code>should not have (length (N))</code>.
   */
  def have(resultOfLengthWordApplication: ResultOfLengthWordApplication)(implicit len: Length[T]): Assertion = {
    val right = resultOfLengthWordApplication.expectedLength
    val leftLength = len.lengthOf(left)
    if ((leftLength == right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.hadLengthInsteadOfExpectedLength(left, leftLength, right)
        else
          FailureMessages.hadLength(left, right)
      )
    }
    else Succeeded
  }

  /**
   * Enables parentheses to be placed around <code>size (N)</code> in expressions of the
   * form: <code>should not have (size (N))</code>.
   */
  def have(resultOfSizeWordApplication: ResultOfSizeWordApplication)(implicit sz: Size[T]): Assertion = {
    val right = resultOfSizeWordApplication.expectedSize
    val leftSize = sz.sizeOf(left)
    if ((leftSize == right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.hadSizeInsteadOfExpectedSize(left, leftSize, right)
        else
          FailureMessages.hadSize(left, right)
      )
    }
    else Succeeded
  }

  // TODO: See about putting U <: T back in here, now that I got rid of the implicit conversion
  // that required me to get rid of the U and just use T. The idea is if I have a phoneBook should have (...)
  // that I could pass HavePropertyMatchers for any supertype of PhoneBook. I could use HavePropertyMatcher[Book]s
  // for example. So write a test and see if that doesn't work, and then if not, go ahead and put the U back.
  // Actually because the T is contravariant, a HavePropertyMatcher[Book] is already a subtype of HavePropertyMatcher[PhoneBook]
  // So I don't need the U <: T anyway. But maybe test to verify this would be worthwhile.
  // The type parameter U has T as its lower bound, which means that U must be T or a supertype of T. Left is T, oh, because
  // HavePropertyMatcher is contravariant in its type parameter T, and that nmakes sense, because a HavePropertyMatcher of Any should
  // be able to match on a String.
  // <code>not have (a (1), b (2))</code> must mean the opposite of <code>have (a (1), b (2))</code>, which means that 
  // <code>not have (a (1), b (2))</code> will be true if either <code>(a (1)).matches</code> or <code>(b (1)).matches</code> is false.
  // Only if both <code>(a (1)).matches</code> or <code>(b (1)).matches</code> are true will <code>not have (a (1), b (2))</code> be false.
  // title/author matches | have | have not
  // 0 0 | 0 | 1 
  // 0 1 | 0 | 1
  // 1 0 | 0 | 1
  // 1 1 | 1 | 0
  // 
  /**
   * This method enables the following syntax, where <code>badBook</code> is, for example, of type <code>Book</code> and
   * <code>title ("One Hundred Years of Solitude")</code> results in a <code>HavePropertyMatcher[Book]</code>:
   *
   * <pre class="stHighlight">
   * book should not have (title ("One Hundred Years of Solitude"))
   *                 ^
   * </pre>
   */
  def have(firstPropertyMatcher: HavePropertyMatcher[T, _], propertyMatchers: HavePropertyMatcher[T, _]*): Assertion = {

    val results =
      for (propertyVerifier <- firstPropertyMatcher :: propertyMatchers.toList) yield
        propertyVerifier(left)

    val firstFailureOption = results.find(pv => !pv.matches)

    val justOneProperty = propertyMatchers.length == 0

    // if shouldBeTrue is false, then it is like "not have ()", and should throw TFE if firstFailureOption.isDefined is false
    // if shouldBeTrue is true, then it is like "not (not have ()), which should behave like have ()", and should throw TFE if firstFailureOption.isDefined is true
    if (firstFailureOption.isDefined == shouldBeTrue) {
      firstFailureOption match {
        case Some(firstFailure) =>
          // This is one of these cases, thus will only get here if shouldBeTrue is true
          // 0 0 | 0 | 1
          // 0 1 | 0 | 1
          // 1 0 | 0 | 1
          throw newTestFailedException(
            FailureMessages.propertyDidNotHaveExpectedValue(
               UnquotedString(firstFailure.propertyName),
               firstFailure.expectedValue,
               firstFailure.actualValue,
               left
            )
          )
        case None =>
          // This is this cases, thus will only get here if shouldBeTrue is false
          // 1 1 | 1 | 0
          val failureMessage =
            if (justOneProperty) {
              val firstPropertyResult = results.head // know this will succeed, because firstPropertyMatcher was required
              FailureMessages.propertyHadExpectedValue(
                UnquotedString(firstPropertyResult.propertyName),
                firstPropertyResult.expectedValue,
                left
              )
            }
            else FailureMessages.allPropertiesHadExpectedValues(left)

          throw newTestFailedException(failureMessage)
      } 
    }
    else Succeeded
  }
  
  def have(resultOfMessageWordApplication: ResultOfMessageWordApplication)(implicit messaging: Messaging[T]): Assertion = {
    val right = resultOfMessageWordApplication.expectedMessage
    val actualMessage = messaging.messageOf(left)
    if ((actualMessage == right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.hadMessageInsteadOfExpectedMessage(left, actualMessage, right)
        else
          FailureMessages.hadExpectedMessage(left, right)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * object should not contain ("one")
   *                   ^
   * </pre>
   */
  def contain(expectedElement: Any)(implicit containing: Containing[T]): Assertion = {
    val right = expectedElement
    if (containing.contains(left, right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainExpectedElement(left, right)
        else
          FailureMessages.containedExpectedElement(left, right)
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * map should not be (null)
   *                ^
   * </pre>
   */
  def be(o: Null)(implicit ev: T <:< AnyRef): Assertion = {
    if ((left == null) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotNull(left)
        else
          FailureMessages.wasNull
      )
    }
    else Succeeded
  }

  // SKIP-SCALATESTJS-START
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * stack should not be ('empty)
   *                  ^
   * </pre>
   */
  def be(symbol: Symbol)(implicit toAnyRef: T <:< AnyRef): Assertion = {
    val matcherResult = matchSymbolToPredicateMethod(toAnyRef(left), symbol, false, false)
    if (matcherResult.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
      )
    }
    else Succeeded
  }
  // SKIP-SCALATESTJS-END

  /**
   * This method enables the following syntax, where <code>stack</code> is, for example, of type <code>Stack</code> and
   * <code>empty</code> refers to a <code>BePropertyMatcher[Stack]</code>:
   *
   * <pre class="stHighlight">
   * stack should not be (empty)
   *                      ^
   * </pre>
   */
  def be(bePropertyMatcher: BePropertyMatcher[T])(implicit ev: T <:< AnyRef): Assertion = {
    val result = bePropertyMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNot(left, UnquotedString(result.propertyName))
        else
          FailureMessages.was(left, UnquotedString(result.propertyName))
      )
    }
    else Succeeded
  }

  // SKIP-SCALATESTJS-START
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * notFileMock should not be a ('file)
   *                        ^
   * </pre>
   */
  def be(resultOfAWordApplication: ResultOfAWordToSymbolApplication)(implicit toAnyRef: T <:< AnyRef): Assertion = {
    val matcherResult = matchSymbolToPredicateMethod(toAnyRef(left), resultOfAWordApplication.symbol, true, true)
    if (matcherResult.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
      )
    }
    else Succeeded
  }
  // SKIP-SCALATESTJS-END

  /**
   * This method enables the following syntax, where <code>notFileMock</code> is, for example, of type <code>File</code> and
   * <code>file</code> refers to a <code>BePropertyMatcher[File]</code>:
   *
   * <pre class="stHighlight">
   * notFileMock should not be a (file)
   *                        ^
   * </pre>
   */
  def be[U >: T](resultOfAWordApplication: ResultOfAWordToBePropertyMatcherApplication[U])(implicit ev: T <:< AnyRef): Assertion = {
    val result = resultOfAWordApplication.bePropertyMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotA(left, UnquotedString(result.propertyName))
        else
          FailureMessages.wasA(left, UnquotedString(result.propertyName))
      )
    }
    else Succeeded
  }

  // SKIP-SCALATESTJS-START
  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * keyEvent should not be an ('actionKey)
   *                     ^
   * </pre>
   */
  def be(resultOfAnWordApplication: ResultOfAnWordToSymbolApplication)(implicit toAnyRef: T <:< AnyRef): Assertion = {
    val matcherResult = matchSymbolToPredicateMethod(toAnyRef(left), resultOfAnWordApplication.symbol, true, false)
    if (matcherResult.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue) matcherResult.failureMessage else matcherResult.negatedFailureMessage
      )
    }
    else Succeeded
  }
  // SKIP-SCALATESTJS-END

  /**
   * This method enables the following syntax, where <code>keyEvent</code> is, for example, of type <code>KeyEvent</code> and
   * <code>actionKey</code> refers to a <code>BePropertyMatcher[KeyEvent]</code>:
   *
   * <pre class="stHighlight">
   * keyEvent should not be an (actionKey)
   *                     ^
   * </pre>
   */
  def be[U >: T](resultOfAnWordApplication: ResultOfAnWordToBePropertyMatcherApplication[U])(implicit ev: T <:< AnyRef): Assertion = {
    val result = resultOfAnWordApplication.bePropertyMatcher(left)
    if (result.matches != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotAn(left, UnquotedString(result.propertyName))
        else
          FailureMessages.wasAn(left, UnquotedString(result.propertyName))
      )
    }
    else Succeeded
  }

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * otherString should not be theSameInstanceAs (string)
   *                        ^
   * </pre>
   */
  def be(resultOfSameInstanceAsApplication: ResultOfTheSameInstanceAsApplication)(implicit toAnyRef: T <:< AnyRef): Assertion = {
    if ((resultOfSameInstanceAsApplication.right eq toAnyRef(left)) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotSameInstanceAs(left, resultOfSameInstanceAsApplication.right)
        else
          FailureMessages.wasSameInstanceAs(left, resultOfSameInstanceAsApplication.right)
      )
    }
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * xs should not be sorted
   *                  ^
   * </pre>
   */
  def be[U](sortedWord: SortedWord)(implicit sortable: Sortable[T]): Assertion = {
    if (sortable.isSorted(left) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotSorted(left)
        else
          FailureMessages.wasSorted(left)
      )
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * file should not be readable
   *                    ^
   * </pre>
   */
  def be[U](readableWord: ReadableWord)(implicit readability: Readability[T]): Assertion = {
    if (readability.isReadable(left) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotReadable(left)
        else
          FailureMessages.wasReadable(left)
      )
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * file should not be writable
   *                    ^
   * </pre>
   */
  def be[U](writableWord: WritableWord)(implicit writability: Writability[T]): Assertion = {
    if (writability.isWritable(left) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotWritable(left)
        else
          FailureMessages.wasWritable(left)
      )
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * list should not be empty
   *                    ^
   * </pre>
   */
  def be[U](emptyWord: EmptyWord)(implicit emptiness: Emptiness[T]): Assertion = {
    if (emptiness.isEmpty(left) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotEmpty(left)
        else
          FailureMessages.wasEmpty(left)
      )
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * option should not be defined
   *                      ^
   * </pre>
   */
  def be[U](definedWord: DefinedWord)(implicit definition: Definition[T]): Assertion = {
    if (definition.isDefined(left) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.wasNotDefined(left)
        else
          FailureMessages.wasDefined(left)
      )
    else Succeeded
  }

  def contain(newOneOf: ResultOfOneOfApplication)(implicit containing: Containing[T]): Assertion = {

    val right = newOneOf.right

    if (containing.containsOneOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainOneOfElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.containedOneOfElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(oneElementOf: ResultOfOneElementOfApplication)(implicit evidence: Containing[T]): Assertion = {

    val right = oneElementOf.right

    if (evidence.containsOneOf(left, right.distinct) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainOneElementOf(left, right)
        else
          FailureMessages.containedOneElementOf(left, right)
      )
    else Succeeded
  }

  def contain(atLeastOneOf: ResultOfAtLeastOneOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = atLeastOneOf.right

    if (aggregating.containsAtLeastOneOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainAtLeastOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.containedAtLeastOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(atLeastOneElementOf: ResultOfAtLeastOneElementOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = atLeastOneElementOf.right

    if (aggregating.containsAtLeastOneOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainAtLeastOneElementOf(left, right)
        else
          FailureMessages.containedAtLeastOneElementOf(left, right)
      )
    else Succeeded
  }

  def contain(noneOf: ResultOfNoneOfApplication)(implicit containing: Containing[T]): Assertion = {

    val right = noneOf.right

    if (containing.containsNoneOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.containedAtLeastOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.didNotContainAtLeastOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(noElementsOf: ResultOfNoElementsOfApplication)(implicit containing: Containing[T]): Assertion = {

    val right = noElementsOf.right

    if (containing.containsNoneOf(left, right.distinct) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.containedAtLeastOneOf(left, right)
        else
          FailureMessages.didNotContainAtLeastOneOf(left, right)
      )
    else Succeeded
  }
  
  def contain(theSameElementsAs: ResultOfTheSameElementsAsApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = theSameElementsAs.right

    if (aggregating.containsTheSameElementsAs(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainSameElements(left, right)
        else
          FailureMessages.containedSameElements(left, right)
      )
    else Succeeded
  }

  def contain(theSameElementsInOrderAs: ResultOfTheSameElementsInOrderAsApplication)(implicit sequencing: Sequencing[T]): Assertion = {

    val right = theSameElementsInOrderAs.right

    if (sequencing.containsTheSameElementsInOrderAs(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainSameElementsInOrder(left, right)
        else
          FailureMessages.containedSameElementsInOrder(left, right)
      )
    else Succeeded
  }
  
  def contain(only: ResultOfOnlyApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = only.right
    if (aggregating.containsOnly(left, right) != shouldBeTrue) {
      val withFriendlyReminder = right.size == 1 && (right(0).isInstanceOf[scala.collection.GenTraversable[_]] || right(0).isInstanceOf[Every[_]])
      throw newTestFailedException(
        if (shouldBeTrue)
          if (withFriendlyReminder)
            FailureMessages.didNotContainOnlyElementsWithFriendlyReminder(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
          else
            FailureMessages.didNotContainOnlyElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          if (withFriendlyReminder)
            FailureMessages.containedOnlyElementsWithFriendlyReminder(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
          else
            FailureMessages.containedOnlyElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    }
    else Succeeded
  }

  def contain(only: ResultOfInOrderOnlyApplication)(implicit sequencing: Sequencing[T]): Assertion = {
    val right = only.right
    if (sequencing.containsInOrderOnly(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainInOrderOnlyElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.containedInOrderOnlyElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }
  
  def contain(allOf: ResultOfAllOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = allOf.right
    if (aggregating.containsAllOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainAllOfElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.containedAllOfElements(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(allElementsOf: ResultOfAllElementsOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = allElementsOf.right
    if (aggregating.containsAllOf(left, right.distinct) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainAllElementsOf(left, right)
        else
          FailureMessages.containedAllElementsOf(left, right)
      )
    else Succeeded
  }
  
  def contain(only: ResultOfInOrderApplication)(implicit sequencing: Sequencing[T]) {

    val right = only.right
    if (sequencing.containsInOrder(left, right) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainAllOfElementsInOrder(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
        else
          FailureMessages.containedAllOfElementsInOrder(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(only: ResultOfInOrderElementsOfApplication)(implicit sequencing: Sequencing[T]): Assertion = {

    val right = only.right
    if (sequencing.containsInOrder(left, right.distinct) != shouldBeTrue)
      throw newTestFailedException(
	if (shouldBeTrue)
	  FailureMessages.didNotContainAllElementsOfInOrder(left, right)
	else
	  FailureMessages.containedAllElementsOfInOrder(left, right)
      )
    else Succeeded
  }
  
  def contain(atMostOneOf: ResultOfAtMostOneOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = atMostOneOf.right

    if (aggregating.containsAtMostOneOf(left, right) != shouldBeTrue)
      throw newTestFailedException(
	if (shouldBeTrue)
	  FailureMessages.didNotContainAtMostOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
	else
	  FailureMessages.containedAtMostOneOf(left, UnquotedString(right.map(FailureMessages.decorateToStringValue).mkString(", ")))
      )
    else Succeeded
  }

  def contain(atMostOneElementOf: ResultOfAtMostOneElementOfApplication)(implicit aggregating: Aggregating[T]): Assertion = {

    val right = atMostOneElementOf.right

    if (aggregating.containsAtMostOneOf(left, right.distinct) != shouldBeTrue)
      throw newTestFailedException(
	if (shouldBeTrue)
	  FailureMessages.didNotContainAtMostOneElementOf(left, right)
	else
	  FailureMessages.containedAtMostOneElementOf(left, right)
      )
    else Succeeded
  }

  def contain(resultOfKeyWordApplication: ResultOfKeyWordApplication)(implicit keyMapping: KeyMapping[T]): Assertion = {
    val right = resultOfKeyWordApplication.expectedKey
    if (keyMapping.containsKey(left, right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainKey(left, right)
        else
          FailureMessages.containedKey(left, right)
      )
    }
    else Succeeded
  }
  def contain(resultOfValueWordApplication: ResultOfValueWordApplication)(implicit valueMapping: ValueMapping[T]): Assertion = {
    val right = resultOfValueWordApplication.expectedValue
    if (valueMapping.containsValue(left, right) != shouldBeTrue) {
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotContainValue(left, right)
        else
          FailureMessages.containedValue(left, right)
      )
    }
    else Succeeded
  }
  
  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should not fullyMatch regex ("""(-)?(\d+)(\.\d*)?""")
   *                   ^
   * </pre>
   *
   * <p>
   * The regular expression passed following the <code>regex</code> token can be either a <code>String</code>
   * or a <code>scala.util.matching.Regex</code>.
   * </p>
   */
  def fullyMatch(resultOfRegexWordApplication: ResultOfRegexWordApplication)(implicit ev: T <:< String): Assertion = {
    val result = fullyMatchRegexWithGroups(left, resultOfRegexWordApplication.regex, resultOfRegexWordApplication.groups)
    if (result.matches != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue) result.failureMessage else result.negatedFailureMessage
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should not include regex ("wo.ld")
   *                   ^
   * </pre>
   *
   * <p>
   * The regular expression passed following the <code>regex</code> token can be either a <code>String</code>
   * or a <code>scala.util.matching.Regex</code>.
   * </p>
   */
  def include(resultOfRegexWordApplication: ResultOfRegexWordApplication)(implicit ev: T <:< String): Assertion = {
    val result = includeRegexWithGroups(left, resultOfRegexWordApplication.regex, resultOfRegexWordApplication.groups)
    if (result.matches != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue) result.failureMessage else result.negatedFailureMessage
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should not include ("world")
   *                   ^
   * </pre>
   */
  def include(expectedSubstring: String)(implicit ev: T <:< String): Assertion = {
    if ((left.indexOf(expectedSubstring) >= 0) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotIncludeSubstring(left, expectedSubstring)
        else
          FailureMessages.includedSubstring(left, expectedSubstring)
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * string should not startWith regex ("Hel*o")
   *                   ^
   * </pre>
   *
   * <p>
   * The regular expression passed following the <code>regex</code> token can be either a <code>String</code>
   * or a <code>scala.util.matching.Regex</code>.
   * </p>
   */
  def startWith(resultOfRegexWordApplication: ResultOfRegexWordApplication)(implicit ev: T <:< String): Assertion = {
    val result = startWithRegexWithGroups(left, resultOfRegexWordApplication.regex, resultOfRegexWordApplication.groups)
    if (result.matches != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue) result.failureMessage else result.negatedFailureMessage
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * "eight" should not startWith ("1.7")
   *                    ^
   * </pre>
   */
  def startWith(expectedSubstring: String)(implicit ev: T <:< String): Assertion = {
    if ((left.indexOf(expectedSubstring) == 0) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotStartWith(left, expectedSubstring)
        else
          FailureMessages.startedWith(left, expectedSubstring)
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * greeting should not endWith regex ("wor.d")
   *                     ^
   * </pre>
   */
  def endWith(resultOfRegexWordApplication: ResultOfRegexWordApplication)(implicit ev: T <:< String): Assertion = {
    val result = endWithRegexWithGroups(left, resultOfRegexWordApplication.regex, resultOfRegexWordApplication.groups)
    if (result.matches != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue) result.failureMessage else result.negatedFailureMessage
      )
    else Succeeded
  }

  /**
   * This method enables the following syntax: 
   *
   * <pre class="stHighlight">
   * "eight" should not endWith ("1.7")
   *                    ^
   * </pre>
   */
  def endWith(expectedSubstring: String)(implicit ev: T <:< String): Assertion = {
    if ((left endsWith expectedSubstring) != shouldBeTrue)
      throw newTestFailedException(
        if (shouldBeTrue)
          FailureMessages.didNotEndWith(left, expectedSubstring)
        else
          FailureMessages.endedWith(left, expectedSubstring)
      )
    else Succeeded
  }

  import scala.language.experimental.macros

  /**
   * This method enables the following syntax:
   *
   * <pre class="stHighlight">
   * result should not matchPattern { case Person("Bob", _) => }
   *                   ^
   * </pre>
   */
  def matchPattern(right: PartialFunction[Any, _]) = macro MatchPatternMacro.matchPattern
  
  /**
   * Overrides toString to return pretty text.
   */
  override def toString: String = "ResultOfNotWordForAny(" + Prettifier.default(left) + ", " + Prettifier.default(shouldBeTrue) + ")"
}

