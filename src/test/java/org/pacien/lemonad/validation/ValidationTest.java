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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pacien.lemonad.attempt.Attempt;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author pacien
 */
class ValidationTest {
  @Test void testValidResult() {
    var subject = "subject";
    var validation = Validation.valid(subject);
    assertTrue(validation.getErrors().isEmpty());
    assertTrue(validation.isValid());
    assertFalse(validation.isInvalid());
    validation.ifValid(innerSubject -> assertEquals(subject, innerSubject));
    validation.ifInvalid((__, ___) -> fail());
    assertEquals(Attempt.success(subject), validation.toAttempt());
  }

  @Test void testInvalidResult() {
    var subject = "subject";
    var errors = List.of(0, 1);
    var validation = Validation.invalid(subject, 0, 1);
    assertEquals(errors, validation.getErrors());
    assertFalse(validation.isValid());
    assertTrue(validation.isInvalid());
    validation.ifValid(Assertions::fail);
    validation.ifInvalid((innerSubject, innerErrors) -> {
      assertEquals(subject, innerSubject);
      assertEquals(errors, innerErrors);
    });
    assertEquals(Attempt.failure(errors), validation.toAttempt());
  }

  @Test void testFlatMap() {
    Validation.valid("subject")
              .ifInvalid((__, ___) -> fail())
              .flatMap(res -> Validation.invalid(res.getSubject(), 0))
              .ifValid(innerSubject -> fail());
  }

  @Test void testMerge() {
    var subject = "subject";
    assertEquals(List.of(0, 1, 2, 3), Validation.merge(subject, Stream.of(
      Validation.valid(subject),
      Validation.invalid(subject, 0, 1),
      Validation.invalid(subject, 2, 3))
    ).getErrors());
  }
}
