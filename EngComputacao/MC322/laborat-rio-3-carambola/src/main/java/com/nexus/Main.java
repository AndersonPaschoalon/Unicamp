package com.nexus;

import java.util.List;

import com.nexus.app.AppBootstrap;
import com.nexus.app.AppContext;

import com.nexus.view.CLIHandlerRegistry;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.NexusCLI;

public class Main {
    public static void main(String[] args) {

        // AppContext é um container para todos os componentes e serviços da aplicação,
        // dentre
        // lá estão o workspace, os serviços de usuário, projeto, tarefa, workflow, etc.
        // Ele é criado no início da aplicação e passado para os handlers do CLI, que
        // precisam desses serviços para funcionar.
        try (AppContext context = AppBootstrap.create()) {

            List<InteractiveCLIHandler> handlers = CLIHandlerRegistry.create(context);

            // O NexusCLI é a interface de linha de comando interativa que o usuário irá
            // usar para
            // interagir com a aplicação. Ele recebe o dispatcher do contexto e a lista de
            // handlers para registrar os comandos disponíveis.
            NexusCLI cli = new NexusCLI(
                    context.getDispatcher(),
                    handlers);

            if (args.length > 0) {
                // Sua lógica atual de processar arquivo...
            }

            // Inicia o loop de comando do CLI, permitindo que o usuário interaja com a
            // aplicação através da linha de comando.
            cli.start();

        } catch (Exception e) {

            System.err.println(
                    "An error occurred: " + e.getMessage());

            e.printStackTrace();
        }
    }
}