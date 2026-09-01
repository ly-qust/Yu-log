<template>
  <RouterView v-slot="{ Component, route }">
    <Transition mode="out-in" name="page">
      <component :is="Component" :key="route.fullPath" />
    </Transition>
  </RouterView>
</template>
