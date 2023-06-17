# MEMORY ALLOCATION VIDEO:
# https://www.youtube.com/watch?v=WBmgZOjEJ6E&t=5s
# Trabalho Prático 2 de Sistemas Operacionais, Prof. Fabiano Passuelo Hessel
# GRUPO X: Guilherme Specht, Dante Flesch, Gabriel Decian e Gabriel Isdra
# Última atualização: 17/06/2023 // 13:49

class ParticaoMemoria:
    def __init__(self, size):
        self.tamanho = size
        # Lista de blocos livres (início, tamanho)
        self.blocos_livres = [(0, size)]
        # Dicionário de blocos alocados (ID do processo: (início, tamanho))
        self.blocos_alocados = {}

    def worst_fit(self, processo_id, tamanho):
        # Ordena os blocos livres pelo tamanho em ordem decrescente
        self.blocos_livres.sort(key=lambda bloco: bloco[1], reverse=True)

        for i, (comeco, tamanho_bloco) in enumerate(self.blocos_livres):
            if tamanho_bloco >= tamanho:
                bloco_alocado = (comeco, tamanho)
                self.blocos_alocados[processo_id] = bloco_alocado

                # Divide o bloco livre em dois: um alocado e outro livre
                if tamanho_bloco > tamanho:
                    self.blocos_livres[i] = (
                        comeco + tamanho, tamanho_bloco - tamanho)
                else:
                    del self.blocos_livres[i]

                return bloco_alocado

        return None

    def circular_fit(self, processo_id, tamanho):
        # Procura espaço para alocar o processo a partir do último bloco alocado
        ultimo_bloco_alocado = None
        for processo, (comeco, tamanho_bloco) in self.blocos_alocados.items():
            if processo == processo_id:
                ultimo_bloco_alocado = (comeco, tamanho_bloco)

        if ultimo_bloco_alocado is not None:
            comeco, tamanho_bloco = ultimo_bloco_alocado
            bloco_seguinte = (comeco + tamanho_bloco) % self.tamanho
            for i, (comeco_livre, tamanho_livre) in enumerate(self.blocos_livres):
                if comeco_livre == bloco_seguinte and tamanho_livre >= tamanho:
                    bloco_alocado = (comeco_livre, tamanho)
                    self.blocos_alocados[processo_id] = bloco_alocado

                    # Divide o bloco livre em dois: um alocado e outro livre
                    if tamanho_livre > tamanho:
                        self.blocos_livres[i] = (
                            comeco_livre + tamanho, tamanho_livre - tamanho)
                    else:
                        del self.blocos_livres[i]

                    return bloco_alocado

        # Caso não seja possível encontrar espaço a partir do último bloco alocado, utiliza o worst-fit
        return self.worst_fit(processo_id, tamanho)


    def desalocar(self, processo_id):
        if processo_id in self.blocos_alocados:
            bloco_liberado = self.blocos_alocados[processo_id]
            del self.blocos_alocados[processo_id]
            self.blocos_livres.append(bloco_liberado)
            self.blocos_livres.sort(key=lambda bloco: bloco[0])
            self.juntar_blocos_livres()

    # Função feita apenas para melhorar a visualização dos blocos livres
    def juntar_blocos_livres(self):
        self.blocos_livres.sort(key=lambda bloco: bloco[0])
        i = 0
        while i < len(self.blocos_livres) - 1:
            bloco_atual = self.blocos_livres[i]
            proximo_bloco = self.blocos_livres[i + 1]
            if bloco_atual[0] + bloco_atual[1] == proximo_bloco[0]:
                tamanho_total = bloco_atual[1] + proximo_bloco[1]
                self.blocos_livres[i] = (bloco_atual[0], tamanho_total)
                del self.blocos_livres[i + 1]
            else:
                i += 1

    def status_memoria(self):
        print("Ocupação da Memória")
        print("Blocos Livres:")
        for comeco, tamanho in self.blocos_livres:
            print(f"({comeco} até {comeco + tamanho - 1}) | {tamanho} |")
        print("Blocos alocados:")
        for process_id, (comeco, tamanho) in self.blocos_alocados.items():
            print(f"({comeco} - {comeco + tamanho - 1}) | {tamanho} |")
        print("")


def main():
    tamanho_memoria = 16
    memoria = ParticaoMemoria(tamanho_memoria)
    estrategia_alocacao = input(
        "Digite a política de alocação (1 - Worst-Fit ou 2 - Circular-Fit): ").lower()

    nome_arquivo = input("Digite o nome completo do arquivo com as requisições: ")
    print(f'\nTamanho da memória |{tamanho_memoria}| posições\n')
    with open(nome_arquivo, "r") as file:
        linhas = file.readlines()

    for linha in linhas:
        linha = linha.strip()
        if linha.startswith("IN"):
            # Comando de alocação de espaço: IN(process_id, size)
            comando, parametros = linha.split("(")
            processo_id, tamanho = parametros[:-1].split(",")
            tamanho = int(tamanho)

            if estrategia_alocacao == "1":
                bloco_alocado = memoria.worst_fit(processo_id, tamanho)
            elif estrategia_alocacao == "2":
                bloco_alocado = memoria.circular_fit(processo_id, tamanho)
            else:
                print("Estratégia de alocação inválida.")
                return

            if bloco_alocado is None:
                print("ESPAÇO INSUFICIENTE DE MEMÓRIA")
            else:
                print(f"Alocado {processo_id} ({parametros})")
        elif linha.startswith("OUT"):
            # Comando de liberação de espaço: OUT(process_id)
            comando, processo_id = linha.split("(")
            processo_id = processo_id[:-1]

            memoria.desalocar(processo_id)
            print(f"Liberado {processo_id}")

        memoria.status_memoria()


if __name__ == "__main__":
    main()

