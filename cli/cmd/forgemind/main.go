package main

import (
	"fmt"
	"os"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: forgemind <command> [arguments]")
		fmt.Println("Commands:")
		fmt.Println("  login          Authenticate with ForgeMind")
		fmt.Println("  project        Manage projects")
		fmt.Println("  task           Manage tasks")
		os.Exit(1)
	}

	command := os.Args[1]
	switch command {
	case "login":
		fmt.Println("Logging into ForgeMind (stub)...")
	case "project":
		fmt.Println("Project management commands (stub)...")
	case "task":
		fmt.Println("Task management commands (stub)...")
	default:
		fmt.Printf("Unknown command: %s\n", command)
		os.Exit(1)
	}
}
