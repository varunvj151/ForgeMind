resource "aws_db_instance" "forgemind_db" {
  identifier        = "forgemind-${var.environment}-postgres"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t4g.large"
  allocated_storage = 100
  storage_type      = "gp3"

  db_name  = "forgemind"
  username = var.db_user
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.db_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name

  multi_az               = true
  publicly_accessible    = false
  storage_encrypted      = true
  skip_final_snapshot    = false
  final_snapshot_identifier = "forgemind-${var.environment}-final-snapshot"

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
}
