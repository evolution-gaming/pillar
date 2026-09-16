package com.evolution.pillar

import java.time.Instant

case class MigrationKey(authoredAt: Instant, description: String) {

  /**
   * Nicer textual representation for logging.
   *
   * Since authoredAt primary identity in migration files is numeric (number of
   * milliseconds since the UNIX epoch), we print the number representation first,
   * followed by a human-readable timestamp representation.
   */
  override def toString: String = {
    s"{authoredAt: ${ authoredAt.toEpochMilli } ($authoredAt), description: '$description'}"
  }
}
