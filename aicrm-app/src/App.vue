<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('AiCRM App launched')
})
</script>

<style>
page {
  background-color: #f5f6fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #1a1a2e;
  font-size: 28rpx;
  line-height: 1.6;
}
.container { padding: 24rpx; }
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 20rpx;
}
.text-secondary { color: #636e7b; }
.text-hint { color: #a0a8b4; }
.text-brand { color: #4F6EF6; }
.text-success { color: #34C759; }
.text-warning { color: #FF9500; }
.text-danger { color: #FF3B30; }
.flex { display: flex; }
.flex-1 { flex: 1; }
.items-center { align-items: center; }
.justify-between { justify-content: space-between; }
.gap-sm { gap: 12rpx; }
.gap-md { gap: 20rpx; }
.mt-sm { margin-top: 12rpx; }
.mt-md { margin-top: 20rpx; }
.mb-md { margin-bottom: 20rpx; }
.btn-primary {
  background: #4F6EF6;
  color: #fff;
  border: none;
  border-radius: 16rpx;
  padding: 20rpx 0;
  text-align: center;
  font-size: 30rpx;
  font-weight: 500;
}
.btn-outline {
  background: #fff;
  color: #4F6EF6;
  border: 2rpx solid #4F6EF6;
  border-radius: 16rpx;
  padding: 18rpx 0;
  text-align: center;
  font-size: 28rpx;
}
.tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
}
.tag-blue { background: #E8EDFF; color: #4F6EF6; }
.tag-green { background: #E8F8EE; color: #34C759; }
.tag-orange { background: #FFF3E0; color: #FF9500; }
.tag-red { background: #FFEBEE; color: #FF3B30; }
.tag-gray { background: #F2F3F5; color: #636e7b; }
.divider { height: 1rpx; background: #eef0f5; margin: 20rpx 0; }
.empty-text { text-align: center; color: #a0a8b4; padding: 80rpx 0; font-size: 28rpx; }
</style>
