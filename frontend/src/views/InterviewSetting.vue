<template>
  <div />
</template>

<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();

onMounted(() => {
  router.replace({ name: "AiInterviewSelect", query: { ...route.query } });
});
</script>
