import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class App {

    static Scanner in = new Scanner(System.in);
    static PrintWriter write;
    static Pattern pattIn = Pattern.compile("^IN\\((\\w+),\\s?(\\d+)\\)$");
    static Pattern pattOut = Pattern.compile("^OUT\\((\\w+)\\)$");
    

    public static void main(String[] args) {
        System.out.println("Digite o tamanho da memoria principal empregada (DEVE SER UM POTENCIA DE 2):");
        int memoriaTamanho = in.nextInt();
        while(memoriaTamanho != Integer.highestOneBit(memoriaTamanho))
        {
            System.out.println("Valor invalido. DEVE SER UMA POTENCIA DE 2");
            memoriaTamanho = in.nextInt();
        }
        System.out.println("Digite a política de alocação (1 - Worst-Fit ou 2 - Circular-Fit): ");
        String politicaSelecionada = in.next();
        in.close();
        gerenciador_memoria gerenciador = gerenciador_memoria.factory(politicaSelecionada, memoriaTamanho);
        try (Stream<String> stream = Files.lines(Paths.get("requisicoes2.txt"))) {
            write = new PrintWriter(new File("out.txt"));
            stream.
            forEach(linha -> {
                Matcher match = pattIn.matcher(linha);
                if(match.matches())
                { 

                    String id = match.group(1);
                    int tamanho = Integer.valueOf(match.group(2));
                    System.out.println("IN " + id + " " + tamanho);
                    gerenciador.alocar(id, tamanho);
                    System.out.println(gerenciador);
                    return;
                }
                match = pattOut.matcher(linha);
                if(match.matches())
                {
                    String id = match.group(1);
                    System.out.println("OUT " + id);
                    gerenciador.desalocar(id);
                    System.out.println(gerenciador);
                    return;
                }
                System.out.println(linha + " does not have matching structure - SKIPPING");
            });
        } catch (Exception e) {
            System.out.println("Problemas no processamento do arquivo de entrada");
            e.printStackTrace();
        }
    }
}
