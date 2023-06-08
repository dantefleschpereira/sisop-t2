class MemoryManager:

    def __init__(self, total_memory):
        self.total_memory = total_memory
        self.free_memory = total_memory
        self.processes = {}
