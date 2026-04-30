SELECT table_name AS table_to_archive
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_type = 'BASE TABLE'
  AND table_name NOT IN ('users', 'expense', 'income')
ORDER BY table_name;

CREATE DATABASE IF NOT EXISTS `expensedb_archive`;

SET FOREIGN_KEY_CHECKS = 0;

RENAME TABLE `expenses` TO `expensedb_archive`.`archived_expenses_manual_backup`;
RENAME TABLE `incomes` TO `expensedb_archive`.`archived_incomes_manual_backup`;

SET FOREIGN_KEY_CHECKS = 1;
