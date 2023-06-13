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
        # Encontra o próximo bloco livre disponível
        posicao_atual = 0
        num_blocos = len(self.blocos_livres)

        while num_blocos > 0:
            comeco, tamanho_bloco = self.blocos_livres[posicao_atual]

            if tamanho_bloco >= tamanho:
                # Aloca o bloco livre para o processo
                bloco_alocado = (comeco, tamanho)
                self.blocos_alocados[processo_id] = bloco_alocado

                # Divide o bloco livre em dois: um alocado e outro livre
                if tamanho_bloco > tamanho:
                    self.blocos_livres[posicao_atual] = (
                        comeco + tamanho, tamanho_bloco - tamanho
                    )
                else:
                    del self.blocos_livres[posicao_atual]

                return bloco_alocado

            # Atualiza a posição para o próximo bloco livre
            posicao_atual = (posicao_atual + 1) % num_blocos
            num_blocos -= 1

        return None
    
    def particao_definida(self, processo_id, tamanho):
        #implementar
        return None

    def desalocar(self, processo_id):
        if processo_id in self.blocos_alocados:
            start, tamanho = self.blocos_alocados[processo_id]
            del self.blocos_alocados[processo_id]

            # Verifica se o bloco liberado pode ser fundido com blocos livres adjacentes
            unir_comeco = None
            unir_tamanho = tamanho

            for i, (comeco_bloco, tamanho_bloco) in enumerate(self.blocos_livres):
                if comeco_bloco + tamanho_bloco == start:  # Bloco livre anterior ao bloco liberado
                    unir_comeco = comeco_bloco
                    unir_tamanho += tamanho_bloco
                    del self.blocos_livres[i]
                    break
                elif start + tamanho == comeco_bloco:  # Bloco livre posterior ao bloco liberado
                    unir_tamanho += tamanho_bloco
                    del self.blocos_livres[i]
                    break

            if unir_comeco is not None:
                self.blocos_livres.append((unir_comeco, unir_tamanho))
            else:
                self.blocos_livres.append((start, tamanho))

            # Ordena os blocos livres por ordem crescente de início
            self.blocos_livres.sort(key=lambda block: block[0])

    def status_memoria(self):
        print("Ocupação da Memória")
        print("Blocos Livres:")
        for comeco, tamanho in self.blocos_livres:
            print(f"({comeco} até {comeco + tamanho - 1}) | {tamanho} |")
        # print("Blocos alocados:")
        # for process_id, (comeco, tamanho) in self.blocos_alocados.items():
        #     print(f"({comeco} - {comeco + tamanho - 1}) | {tamanho} |")
        print("")


def main():
    tamanho_memoria = 16
    memoria = ParticaoMemoria(tamanho_memoria)
    estrategia_alocacao = input(
        "Digite a política de alocação (Worst-Fit ou Circular-Fit): ").lower()
    nome_arquivo = input(
        "Digite o nome completo do arquivo com as requisições: ")
    com_buddy = input(
        "Partições variáveis (1) ou partições definidas com o sistema buddy (2): ")
    
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

            if estrategia_alocacao == "worst-fit" and com_buddy == '1':
                bloco_alocado = memoria.worst_fit(processo_id, tamanho)
            elif estrategia_alocacao == "circular-fit" and com_buddy == '1':
                bloco_alocado = memoria.circular_fit(processo_id, tamanho)
            else:
                print("Estratégia de alocação inválida.")
                return
            if com_buddy == '2':
                bloco_alocado = memoria.particao_definida(processo_id, tamanho)
            else:
                print("Problema com o buddy")
            if bloco_alocado is None:
                print("ESPAÇO INSUFICIENTE DE MEMÓRIA")
            else:
                print(f"Alocado {processo_id} ({parametros}")
                # print(f"Alocado {processo_id}: {bloco_alocado[0]} - {bloco_alocado[0] + bloco_alocado[1] - 1}")
        elif linha.startswith("OUT"):
            # Comando de liberação de espaço: OUT(process_id)
            comando, processo_id = linha.split("(")
            processo_id = processo_id[:-1]

            memoria.desalocar(processo_id)
            print(f"Liberado {processo_id}")

        memoria.status_memoria()


if __name__ == "__main__":
    main()
