import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
    console.log('ForgeMind extension is now active!');

    let disposable = vscode.commands.registerCommand('forgemind.startChat', () => {
        vscode.window.showInformationMessage('Hello from ForgeMind AI Assistant!');
        // Implementation will load a WebviewPanel and connect to API v2
    });

    context.subscriptions.push(disposable);
}

export function deactivate() {}
