<template>
  <div class="border rounded bg-white p-4">
    <div class="h-64 overflow-auto mb-2" ref="scroll">
      <div v-for="(m, idx) in messages" :key="idx" class="mb-2">
        <div :class="m.sender === 'AI' ? 'text-left' : 'text-right'">
          <span
            class="inline-block p-2 rounded"
            :class="
              m.sender === 'AI' ? 'bg-gray-100' : 'bg-orange-500 text-white'
            "
            >{{ m.text }}</span
          >
        </div>
      </div>
    </div>
    <div class="flex">
      <el-input
        v-model="input"
        placeholder="请输入回答"
        @keyup.enter="send"
      ></el-input>
      <el-button type="primary" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "Chat",
  data() {
    return {
      messages: [{ sender: "AI", text: "请介绍一下你自己" }],
      input: "",
    };
  },
  methods: {
    async send() {
      if (!this.input) return;
      const text = this.input;
      this.messages.push({ sender: "USER", text });
      this.input = "";
      // 占位：AI思考中
      const placeholder = { sender: "AI", text: "AI思考中...", loading: true };
      this.messages.push(placeholder);
      this.$nextTick(() => {
        const el = this.$refs.scroll;
        el.scrollTop = el.scrollHeight;
      });
      try {
        const { sendUserMessage } = await import("../api/ai");
        const reply = await sendUserMessage(text);
        const idx = this.messages.indexOf(placeholder);
        if (idx !== -1)
          this.messages.splice(idx, 1, { sender: "AI", text: reply });
      } catch (e) {
        const idx = this.messages.indexOf(placeholder);
        if (idx !== -1)
          this.messages.splice(idx, 1, {
            sender: "AI",
            text: "网络异常，请重试",
          });
      } finally {
        this.$nextTick(() => {
          const el = this.$refs.scroll;
          el.scrollTop = el.scrollHeight;
        });
      }
    },
  },
};
</script>
