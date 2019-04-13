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

import java.util.List;

import lombok.NonNull;
import lombok.Value;

import static java.util.Collections.unmodifiableList;

/**
 * @author pacien
 */
@Value class ValidationContainer<S, E> implements Validation<S, E> {
  S subject;
  @NonNull List<E> errors;

  @Override public boolean isValid() {
    return errors.isEmpty();
  }

  @Override public boolean isInvalid() {
    return !isValid();
  }

  @Override public List<E> getErrors() {
    return unmodifiableList(errors);
  }
}
