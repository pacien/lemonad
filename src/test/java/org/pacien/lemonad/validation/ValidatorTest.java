/*
 * lemonad - Some functional sweetness for Java
 * Copyright (C) 2019  Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.lemonad.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author pacien
 */
class ValidatorTest {
  @Test void testValidatorEnsuringPredicate() {
    var emptyError = 0;
    var validator = Validator.ensuringPredicate(not(String::isEmpty), emptyError);
    assertEquals(List.of(emptyError), validator.validate("").getErrors());
    assertEquals(List.of(), validator.validate("test").getErrors());
  }

  @Test void testValidatorValidatingAll() {
    var emptyError = 0;
    var tooLongError = 1;
    var containsBadLetterError = 2;

    var validator = Validator.validatingAll(
      Validator.ensuringPredicate(not(String::isEmpty), emptyError),
      Validator.ensuringPredicate((String str) -> str.length() < 10, tooLongError),
      Validator.ensuringPredicate((String str) -> !str.contains("e"), containsBadLetterError));

    assertEquals(List.of(emptyError), validator.validate("").getErrors());
    assertEquals(List.of(tooLongError, containsBadLetterError), validator.validate("test test test").getErrors());
    assertEquals(List.of(), validator.validate("potato").getErrors());
  }

  @Test void testValidatingField() {
    var emptyError = 0;
    var fieldValidator = Validator.ensuringPredicate((Integer len) -> len > 0, emptyError);
    var validator = Validator.validatingField(String::length, fieldValidator);
    assertEquals(List.of(emptyError), validator.validate("").getErrors());
    assertEquals(List.of(), validator.validate("test").getErrors());
  }
}
